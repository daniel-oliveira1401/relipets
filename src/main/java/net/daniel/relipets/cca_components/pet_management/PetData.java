package net.daniel.relipets.cca_components.pet_management;

import dev.onyxstudios.cca.internal.entity.CardinalEntityInternals;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.progression.LevelProgression;
import net.daniel.relipets.entity.cores.progression.StatsEnum;
import net.daniel.relipets.entity.cores.progression.StatsOperationEnum;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Optional;

public class PetData implements ISerializable {

    public static final String PET_INFO_KEY = "pet_info";
    public static final String HEALING_COOLDOWN = "pet_healing_cooldown";


    public static final String SUMMONED = "summoned";
    public static final String RECALLED = "recalled";
    public static final String HEALING = "healing";

    @Getter
    @Setter
    int healingCooldown = 0;

    @Getter
    @Setter
    private PetEntityData petEntityData;
    @Getter
    String summonState = RECALLED;
    public int petLevel = 0;

    @Getter
    @Setter
    public PetInfo petInfo = new PetInfo();

    public void changeStatPoint(StatsOperationEnum operation, StatsEnum stat, World world){
        if(this.isSummoned()){
            PetMetadataComponent petMetadataComponent = this.getPetEntityData().getMetadata(world);

            if(petMetadataComponent != null){
                int currentLevel = petMetadataComponent.getLevelProgression().getCurrentLevel();
                int totalPointsUsed = petMetadataComponent.getStatUpgrades().getTotalPointsUsed();

                switch (operation){
                    case INCREASE:
                        if(totalPointsUsed < currentLevel){
                            petMetadataComponent.getStatUpgrades().addStatPoint(stat);
                        }
                        break;

                    case DECREASE:
                        if(petMetadataComponent.getStatUpgrades().getStatValue(stat) > 0){
                            petMetadataComponent.getStatUpgrades().removeStatPoint(stat);
                        }
                        break;
                }

                CardinalComponentsRegistry.PET_METADATA_KEY.sync(this.getPetEntityData().getEntity());
                this.getPetEntityData().applyStatModifiers(this.getPetEntityData().getEntity());
            }
        }
    }

    private boolean needsEntityBinding(){

        return this.petEntityData != null && //has entity data
                this.petEntityData.isValid() && //the data is valid
                this.summonState.equals(SUMMONED) && //the pet is summoned somewhere in the world
                this.petEntityData.entity == null; //but it is not bound to the entity data

    }

    public void tick(ServerWorld world, PlayerEntity player){

        handleEntityBindingIfNeeded(world, player);

        updateTrackerIfNeeded(world);

        tickHealingIfNeeded();

        tickSimulatedBehaviorIfPossible(player);

    }

    private void tickSimulatedBehaviorIfPossible(PlayerEntity player) {
        if(this.isSummoned()){

            setPetTargetForRevengeIfApplicable(player);
            //follow owner
            followOwner(player);

            retributeHostilityIfApplicable(player);

            clearPetTargetIfTargetIsOwnerOrPartyMember(player);

        }
    }
    int followDistance = 5;
    int teleportDistance = 25;
    public void followOwner(PlayerEntity player){
        LivingEntity entity = this.getPetEntityData().getEntity();
        if(!(entity instanceof BaseCore) && entity instanceof PathAwareEntity pathAwareEntity){
            double distance = pathAwareEntity.squaredDistanceTo(player);
            if(!pathAwareEntity.getNavigation().isFollowingPath() && distance > (this.teleportDistance * this.teleportDistance)){
                pathAwareEntity.teleport(
                        player.getX(), player.getY(), player.getZ()
                );
            }else if(!pathAwareEntity.getNavigation().isFollowingPath() && distance > (this.followDistance * this.followDistance)){
                FuzzyTargeting.findTo(pathAwareEntity, 5, 3, player.getPos());
                pathAwareEntity.getNavigation().startMovingTo(player, 1.0f);

            }
        }
    }

    private void retributeHostilityIfApplicable(PlayerEntity player) {
        if(targetCooldown <= 0 && this.isSummoned() && !(this.getPetEntityData().getEntity() instanceof BaseCore)){

            this.targetCooldown = 10;

            LivingEntity petEntity = this.getPetEntityData().getEntity();

            if(petEntity.getAttacking() == null || !petEntity.isAlive()) return;
            //for each entity around the player

                //check if their target is the player. If so, break out of the loop and set that
                //entity as the pets target

            List<LivingEntity> hostileEntities = player.getWorld()
                    .getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), player.getBoundingBox().expand(20), (entity)-> {
                Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(entity);
                if(petMetadataComponent.isPresent() && !petMetadataComponent.get().getPlayerUUID().isEmpty()){
                    return false;
                }
                if(entity instanceof MobEntity mobEntity){
                    return mobEntity.getTarget() == player;
                }
                return entity.getAttacking() == player;
            } );

            if(hostileEntities.isEmpty()) return;

            LivingEntity hostile = hostileEntities.get(((int) (Math.random() * hostileEntities.size())));

            petEntity.setAttacker(hostile);

            if(petEntity instanceof MobEntity mobPetEntity){
                mobPetEntity.setTarget(hostile);
            }

            if(petEntity instanceof Angerable angerablePetEntity){
                angerablePetEntity.setAngryAt(hostile.getUuid());
                angerablePetEntity.setTarget(hostile);
            }
        }
        targetCooldown = Math.max(0, targetCooldown - 1);
    }

    int targetCooldown = 0;
    private void setPetTargetForRevengeIfApplicable(PlayerEntity player) {
        if(targetCooldown <= 0 && this.isSummoned() && !(this.getPetEntityData().getEntity() instanceof BaseCore)){
            this.targetCooldown = 10;
            LivingEntity ownerAttacker = player.getAttacker();

            if(ownerAttacker == null){
                ownerAttacker = player.getAttacking();
            }

            if(ownerAttacker == null) return;

            //target to attack must not be a pet in the party
            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            if(petOwnerComponent.getPetParty().getPetByEntityUUID(ownerAttacker.getUuidAsString()) != null) return;

            LivingEntity petEntity = this.getPetEntityData().getEntity();

            petEntity.setAttacker(ownerAttacker);

            if(petEntity instanceof MobEntity mobPetEntity){
                mobPetEntity.setTarget(ownerAttacker);
            }

            if(petEntity instanceof Angerable angerablePetEntity){
                angerablePetEntity.setAngryAt(ownerAttacker.getUuid());
                angerablePetEntity.setTarget(ownerAttacker);
            }
        }
        targetCooldown = Math.max(0, targetCooldown - 1);
    }

    private void clearPetTargetIfTargetIsOwnerOrPartyMember(PlayerEntity player){
        //dont target owner
        LivingEntity petEntity = this.getPetEntityData().getEntity();

        //check "attacking"
        LivingEntity petTarget = petEntity.getAttacking();

        //check "target"
        if(petTarget == null && petEntity instanceof MobEntity mobPetEntity){
            petTarget = mobPetEntity.getTarget();
        }
        if(petTarget != null){
            Optional<PetMetadataComponent> targetMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(petTarget);

            if(petTarget == player || (targetMetadata.isPresent() && !targetMetadata.get().getPlayerUUID().isEmpty())){
                petEntity.setAttacking(null);
                petEntity.setAttacker(null);

                if(petEntity instanceof MobEntity mobPetEntity){
                    mobPetEntity.setTarget(null);
                }

                if(petEntity instanceof Angerable angerablePetEntity){
                    angerablePetEntity.forgive(player);
                    angerablePetEntity.stopAnger();
                }

            }

        }

    }

    private void tickHealingIfNeeded() {
        if(this.isHealing()){
            this.healingCooldown = Math.max(this.healingCooldown - 1, 0);

            if(this.healingCooldown == 0){
                this.summonState = RECALLED;
                Relipets.LOGGER.debug("Pet is healed!");
            }
        }
    }

    public void updateVolatilePetInfoIfPossible(){
        if(this.isSummoned()){
            this.getPetInfo().setPetName(this.getPetEntityData().getEntity().getDisplayName().getString());
            this.getPetInfo().setMaxHealth((int) this.getPetEntityData().getEntity().getMaxHealth());
            this.getPetInfo().setCurrentHealth((int) this.getPetEntityData().getEntity().getHealth());

            PetMetadataComponent component = CardinalComponentsRegistry.PET_METADATA_KEY.get(this.getPetEntityData().getEntity());

            this.getPetInfo().setLevelProgression(component.getLevelProgression());
        }
    }

    public boolean isSummoned(){
        boolean summonStateSummoned = this.summonState.equals(SUMMONED);

        boolean validEntityData = this.getPetEntityData().isValid();

        boolean hasEntity = this.getPetEntityData().entity != null;

        boolean entityIsAlive = hasEntity && this.getPetEntityData().getEntity().isAlive();

        return summonStateSummoned && validEntityData && hasEntity && entityIsAlive;
    }

    /***
     * This should only be used in the client
     * @return
     */
    public boolean isSummonedNoEntityValidation(){
        boolean summonStateSummoned = this.summonState.equals(SUMMONED);

        boolean validEntityData = this.getPetEntityData().isValid();

        return summonStateSummoned && validEntityData;
    }

    public boolean isRecalled(){
        boolean summonStateRecalled = this.summonState.equals(RECALLED);

        boolean validEntityData = this.getPetEntityData().isValid();

        return summonStateRecalled && validEntityData;
    }

    public boolean isHealing(){
        boolean summonStateHealing = this.summonState.equals(HEALING);

        boolean validEntityData = this.getPetEntityData().isValid();

        return summonStateHealing && validEntityData;
    }

    public void summon(ServerWorld world, Vec3d pos, PlayerEntity player){
        this.summonState = SUMMONED;
        this.getPetEntityData().spawnEntity(world, pos, player);
        this.getPetEntityData().setOwner(player);
        System.out.println("spawned entity with health:" + this.getPetEntityData().getEntity().getMaxHealth());
    }

    public void recall(ServerWorld world){

        boolean recalled = this.getPetEntityData().recallEntity(world, (e)-> {
            this.summonState = RECALLED;
            return true;
        });

        if(recalled)
            this.summonState = RECALLED;

    }

    public void teleport(Vec3d pos){
        if(this.isSummoned()){
            this.getPetEntityData().getEntity().teleport(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ()
            );
            //is this a no no?
            BrainUtils.clearMemory(this.getPetEntityData().getEntity(), MemoryModuleType.WALK_TARGET);
            if(this.getPetEntityData().getEntity() instanceof PathAwareEntity entity)
                entity.getNavigation().stop();
        }
    }

    public void fillFromEntity(LivingEntity entity, PlayerEntity player){
        this.summonState = SUMMONED;
        this.petLevel = 0;
        this.setPetEntityData(new PetEntityData());
        this.getPetEntityData().setEntity(entity);
        this.getPetEntityData().updateTracker();
        this.getPetEntityData().saveEntityData();
        this.getPetEntityData().setOwner(player);
    }

    public void onFaint(LivingEntity entity, ServerWorld world){
        entity.setHealth(entity.getMaxHealth());
        entity.clearStatusEffects();
        entity.setOnFire(false);
        entity.setVelocity(0, 0, 0 );
        this.recall(world);
        this.summonState = HEALING;
        this.healingCooldown = 800;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        if(nbt.getKeys().isEmpty()) return;

        if(nbt.contains(RelipetsConstantsRegistry.PET_NBT_KEY)){
            PetEntityData entityData = new PetEntityData();
            entityData.readFromNbt(nbt.getCompound(RelipetsConstantsRegistry.PET_NBT_KEY));
            this.petEntityData = entityData;
        }
        if(nbt.contains(RelipetsConstantsRegistry.PET_SUMMON_STATE_KEY)){
            this.summonState = nbt.getString(RelipetsConstantsRegistry.PET_SUMMON_STATE_KEY);
        }

        if(nbt.contains(RelipetsConstantsRegistry.PET_LEVEL_KEY)){
            this.petLevel = nbt.getInt(RelipetsConstantsRegistry.PET_LEVEL_KEY);
        }

        if(nbt.contains(PET_INFO_KEY)){
            this.petInfo = new PetInfo();
            this.petInfo.readFromNbt(nbt.getCompound(PET_INFO_KEY));
        }

        if(nbt.contains(HEALING_COOLDOWN)){
            this.setHealingCooldown(nbt.getInt(HEALING_COOLDOWN));

        }

        pendingEntityBind = this.needsEntityBinding();
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();

        nbt.putString(RelipetsConstantsRegistry.PET_SUMMON_STATE_KEY, this.summonState);
        nbt.putInt(RelipetsConstantsRegistry.PET_LEVEL_KEY, this.petLevel);
        nbt.putInt(HEALING_COOLDOWN, this.getHealingCooldown());
        if(this.petEntityData != null){
            nbt.put(RelipetsConstantsRegistry.PET_NBT_KEY, this.petEntityData.writeToNbt());

        }

        nbt.put(PET_INFO_KEY, this.petInfo.writeToNbt());

        return nbt;
    }

    public void addHighlight() {

        this.getPetEntityData().getEntity().setGlowing(true);

        Utils.setTimeout(()-> {
            if(this.getPetEntityData().getEntity() != null){
                this.getPetEntityData().getEntity().setGlowing(false);
            }
        }, 10);

    }

    public void applyNaturalHealing() {
        //TODO: everyone heals the same for now. Make it dependent on other factors in the future
        int healing = 1;

        LivingEntity pet = this.getPetEntityData().getEntity();
        pet.setHealth(
                Math.min(pet.getMaxHealth(), pet.getHealth() + healing)
        );
    }


    private void updateTrackerIfNeeded(ServerWorld world){
        if(this.isSummoned()){
            this.petEntityData.updateTracker();
        }
    }

    boolean pendingEntityBind = false;
    private void handleEntityBindingIfNeeded(ServerWorld world, PlayerEntity player){
        if(pendingEntityBind){
            Relipets.LOGGER.debug("Entity needs binding. Trying to bind it");
            System.out.println("Entity needs binding, trying to bind it...");
            this.getPetEntityData().bindEntity(world, player);
            pendingEntityBind = false;
        }
    }


    @Getter
    @Setter
    public static class PetInfo implements ISerializable{

        public static final String LEVEL_PROGRESSION_KEY = "level_progression";

        public static final String NAME_KEY = "pet_info_name";
        public static final String CURRENT_HEALTH_KEY = "pet_info_current_health";
        public static final String MAX_HEALTH_KEY = "pet_info_max_health";

        private String petName = "";
        private int currentHealth = 0;
        private int maxHealth = 0;

        LevelProgression levelProgression;

        @Override
        public void readFromNbt(NbtCompound nbt) {
            if(nbt.contains(NAME_KEY))
                this.setPetName(nbt.getString(NAME_KEY));

            if(nbt.contains(CURRENT_HEALTH_KEY))
                this.setCurrentHealth(nbt.getInt(CURRENT_HEALTH_KEY));

            if(nbt.contains(MAX_HEALTH_KEY))
                this.setMaxHealth(nbt.getInt(MAX_HEALTH_KEY));

            this.levelProgression = new LevelProgression(nbt.getCompound(LEVEL_PROGRESSION_KEY));
        }

        @Override
        public NbtCompound writeToNbt() {

            NbtCompound nbt = new NbtCompound();
            nbt.putString(NAME_KEY, this.getPetName());
            nbt.putInt(CURRENT_HEALTH_KEY, this.getCurrentHealth());
            nbt.putInt(MAX_HEALTH_KEY, this.getMaxHealth());

            if(this.getLevelProgression() != null){
                nbt.put(LEVEL_PROGRESSION_KEY, this.getLevelProgression().writeToNbt());
            }

            return nbt;
        }

    }

}
