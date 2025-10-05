package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PartSystem;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.cca_components.pet_management.progression.LevelProgression;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.Optional;

public class PetData implements ISerializable {

    public static final String PET_INFO_KEY = "pet_info";
    public static final String HEALING_COOLDOWN = "pet_healing_cooldown";
    public static final String NATURAL_HEALING_KEY = "natural_healing";
    public static final String MOVEMENT_MODE_KEY = "movement_mode";

    public static final String SUMMONED = "summoned";
    public static final String RECALLED = "recalled";
    public static final String HEALING = "healing";
    public static final int BASE_FOLLOW_DISTANCE = 10;
    public static final int TP_DISTANCE_DELTA = 15;

    @Getter
    @Setter
    int naturalHealing = 1;
    @Getter
    public static final int BASE_NATURAL_HEALING = 1;

    @Getter
    @Setter
    int healingCooldown = 0;

    @Getter
    @Setter
    private PetEntityData petEntityData;
    @Getter
    String summonState = RECALLED;

    @Getter
    @Setter
    public PetInfo petInfo = new PetInfo();

    @Getter
    @Setter
    PetMoveMode moveMode = PetMoveMode.FOLLOWING;

    public void changeStatPoint(PetParty party, StatsOperationEnum operation, StatsEnum stat, ServerWorld world){
        if(this.isSummoned()){
            LivingEntity entity = this.getPetEntityData().getEntity(world.getServer());
            if(entity == null){
                Utils.log("Could not find entity to retrieve metadata from.");
                return;
            }
            PetMetadataComponent petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.get(entity);

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

            this.getPetEntityData().applyStatModifiers(entity, this);
            this.getPetEntityData().saveEntityData(world.getServer());
            CardinalComponentsRegistry.PET_METADATA_KEY.sync(entity);
            party.pushChangesToClient();
        }
    }

    public void tick(PlayerEntity player){

        LivingEntity entity = this.getPetEntityData().getEntity(player.getServer());

        updateTrackerIfNeeded(entity);

        tickHealingIfNeeded();

        tickSimulatedBehaviorIfPossible(entity, player);

    }

    private void tickSimulatedBehaviorIfPossible(LivingEntity entity, PlayerEntity player) {
        if(this.isSummoned()){

            if(entity == null) return;

            setPetTargetForRevengeIfApplicable(entity, player);
            //follow owner
            if(moveMode == PetMoveMode.FOLLOWING){
                followOwner(entity, player);
            }

            retributeHostilityIfApplicable(entity, player);

            clearPetTargetIfTargetIsOwnerOrPartyMember(entity, player);

        }
    }
    int followDistance = 10;
    int teleportDistance = 30;
    public void followOwner(LivingEntity entity, PlayerEntity player){
        if(!(entity instanceof BaseCore) && entity instanceof MobEntity mobEntity){
            boolean sameDimension = entity.getWorld().getDimensionEntry().getKey().get().getValue().compareTo(player.getWorld().getDimensionEntry().getKey().get().getValue()) == 0;
            if(!sameDimension) return;
            double distance = mobEntity.squaredDistanceTo(player);
            if(distance > (this.teleportDistance * this.teleportDistance)){
                mobEntity.getNavigation().stop();
                //recall pets if it is not safe to teleport
                BlockPos safePosToTeleport = Utils.findRandomSafePositionAroundPlayer(entity, (ServerWorld) player.getWorld(), player.getBlockPos(), 8, player.getWorld().getRandom());
                if(safePosToTeleport != null){
                    mobEntity.teleport(
                            safePosToTeleport.getX() + 0.5,
                            safePosToTeleport.getY(),
                            safePosToTeleport.getZ() + 0.5, false
                    );
                }else{
                    mobEntity.teleport(
                            player.getX(),
                            player.getY(),
                            player.getZ(), false
                    );
                }
            }else if(distance > (this.followDistance * this.followDistance)){
                BlockPos pos = Utils.findRandomSafePositionAroundPlayer(entity, (ServerWorld) player.getWorld(), player.getBlockPos(), 15, player.getWorld().getRandom());

                if(pos != null){
                    mobEntity.getMoveControl().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.5f);
                }

            }
        }
    }

    private void retributeHostilityIfApplicable(LivingEntity petEntity, PlayerEntity player) {
        if(targetCooldown <= 0 && this.isSummoned()){

            this.targetCooldown = 10;

            if(petEntity == null) return;
            if(petEntity.getAttacking() != null && petEntity.getAttacking().isAlive()) return;


            List<LivingEntity> hostileEntities = player.getWorld()
                    .getEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), player.getBoundingBox().expand(60), (entity)-> {
                Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(entity);
                if(petMetadataComponent.isPresent() && !petMetadataComponent.get().getPlayerUUID().isEmpty()){
                    return false;
                }
                if(entity instanceof MobEntity mobEntity){
                    return mobEntity.getTarget() == player;
                }
                return entity.getAttacking() == player || (BrainUtils.hasMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET) && BrainUtils.getMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET) == player);
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

            if(BrainUtils.hasMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET)){
                BrainUtils.setMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET, hostile);
            }

        }
        targetCooldown = Math.max(0, targetCooldown - 1);
    }

    int targetCooldown = 0;
    private void setPetTargetForRevengeIfApplicable(LivingEntity petEntity, PlayerEntity player) {
        if(targetCooldown <= 0 && this.isSummoned()){
            this.targetCooldown = 2;
            LivingEntity ownerAttacker = player.getAttacker();

            if(ownerAttacker == null){
                ownerAttacker = player.getAttacking();
            }

            if(ownerAttacker == null || ownerAttacker.isRemoved() || !ownerAttacker.isAlive()) return;

            //target to attack must not be a pet in the party
            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            if(petOwnerComponent.getPetParty().getPetByEntityUUID(ownerAttacker.getUuidAsString()) != null) return;

            if(petEntity == null) return;
            petEntity.setAttacker(ownerAttacker);

            if(petEntity instanceof MobEntity mobPetEntity){
                mobPetEntity.setTarget(ownerAttacker);
            }

            if(petEntity instanceof Angerable angerablePetEntity){
                angerablePetEntity.setAngryAt(ownerAttacker.getUuid());
                angerablePetEntity.setTarget(ownerAttacker);
            }
            if(BrainUtils.hasMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
                BrainUtils.setMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET, ownerAttacker);
            }

        }
        targetCooldown = Math.max(0, targetCooldown - 1);
    }

    private void clearPetTargetIfTargetIsOwnerOrPartyMember(LivingEntity petEntity, PlayerEntity player){
        //dont target owner
        if(petEntity == null) return;
        //check "attacking"
        LivingEntity petTarget = petEntity.getAttacking();

        //check "target"
        if(petTarget == null && petEntity instanceof MobEntity mobPetEntity){
            petTarget = mobPetEntity.getTarget();
        }
        //check brain
        if(petTarget == null && BrainUtils.hasMemory(petEntity, MemoryModuleType.ATTACK_TARGET)){
            petTarget = BrainUtils.getTargetOfEntity(petEntity);
        }

        if(petTarget != null){
            Optional<PetMetadataComponent> targetMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(petTarget);

            if(petTarget == player || !petTarget.isAlive() || petTarget.isRemoved() || (targetMetadata.isPresent() && !targetMetadata.get().getPlayerUUID().isEmpty())){
                petEntity.setAttacking(null);
                petEntity.setAttacker(null);

                if(petEntity instanceof MobEntity mobPetEntity){
                    mobPetEntity.setTarget(null);
                }

                if(petEntity instanceof Angerable angerablePetEntity){
                    angerablePetEntity.forgive(player);
                    angerablePetEntity.stopAnger();
                }
                if(BrainUtils.hasMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET)) {
                    BrainUtils.clearMemory(petEntity.getBrain(), MemoryModuleType.ATTACK_TARGET);
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

    public void updateVolatilePetInfoIfPossible(MinecraftServer server){
        if(this.isSummoned()){
            LivingEntity entity = this.getPetEntityData().getEntity(server);
            if(entity != null){
                this.updateVolatilePetInfo(entity);
            }
        }
    }

    public void updateVolatilePetInfoSync(LivingEntity entity){
        if(this.isSummoned() && entity != null){

            this.updateVolatilePetInfo(entity);

        }
    }

    private void updateVolatilePetInfo(LivingEntity entity){
        this.getPetInfo().setPetName(entity.getDisplayName().getString());
        this.getPetInfo().setMaxHealth((int) entity.getMaxHealth());
        this.getPetInfo().setCurrentHealth((int) entity.getHealth());
        PetMetadataComponent component = CardinalComponentsRegistry.PET_METADATA_KEY.get(entity);
        this.getPetInfo().setLevelProgression(component.getLevelProgression());
    }

    public boolean isSummoned(){
        boolean summonStateSummoned = this.summonState.equals(SUMMONED);

        boolean validEntityData = this.getPetEntityData().isValid();

        //boolean hasEntity = this.getPetEntityData().entity != null;

        //boolean entityIsAlive = hasEntity && this.getPetEntityData().getEntity().isAlive();

        return summonStateSummoned && validEntityData;
                //&& hasEntity;
                //&& entityIsAlive;
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

    public void summon(PetParty party, ServerWorld world, Vec3d pos, PlayerEntity player){
        if(this.isRecalled()){
            this.summonState = SUMMONED;
            this.getPetEntityData().spawnEntity(world, pos, player, this);
            party.pushChangesToClient();
        }else if(this.isHealing()){
            Utils.message(this.getPetInfo().getPetName() + " is healing. Wait "+ Utils.tickToSecond(this.getHealingCooldown()) + "s.", player);

        }
    }

    public void forceSummon(ServerWorld world, Vec3d pos, PlayerEntity player){
        this.summonState = SUMMONED;
        this.getPetEntityData().spawnEntity(world, pos, player, this);
    }

    public void recall(PetParty party, ServerWorld world, PlayerEntity player){
        if(this.isSummoned()){
            boolean recalled = this.getPetEntityData().recallEntity(party, this, world, player, (e)-> {
                this.summonState = RECALLED;
                party.pushChangesToClient();
                return true;
            });

            if(recalled)
                this.summonState = RECALLED;
        }

    }

    public void recallEntitySync(LivingEntity entity, PetData petData, PetParty party, ServerWorld world, PlayerEntity player){
        if(this.isSummoned()){
            this.getPetEntityData().recallSync(player.getServer(), petData, entity, player);
            this.summonState = RECALLED;
        }

    }

    public void fillFromEntity(LivingEntity entity, PlayerEntity player){
        this.summonState = SUMMONED;
        this.setPetEntityData(new PetEntityData());
        this.getPetEntityData().updateTracker(entity);
        this.getPetEntityData().saveEntityData(entity);
        this.getPetEntityData().setOwner(player, entity);
    }

    public void onFaint(PetParty party, LivingEntity entity, ServerWorld world, PlayerEntity player){
        entity.setHealth(entity.getMaxHealth());
        entity.clearStatusEffects();
        entity.setOnFire(false);
        entity.setVelocity(0, 0, 0 );
        this.recallEntitySync(entity,this, party, world, player);
        this.summonState = HEALING;
        this.healingCooldown = (this.getPetInfo().getMaxHealth() / this.getNaturalHealing()) * Utils.secondToTick(1);
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

        if(nbt.contains(NATURAL_HEALING_KEY))
            this.naturalHealing = nbt.getInt(NATURAL_HEALING_KEY);

        if(nbt.contains(PET_INFO_KEY)){
            this.petInfo = new PetInfo();
            this.petInfo.readFromNbt(nbt.getCompound(PET_INFO_KEY));
        }

        if(nbt.contains(HEALING_COOLDOWN)){
            this.setHealingCooldown(nbt.getInt(HEALING_COOLDOWN));

        }

        if(nbt.contains(MOVEMENT_MODE_KEY)){
            this.setMoveMode(PetMoveMode.valueOf(nbt.getString(MOVEMENT_MODE_KEY)));
        }

    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();

        nbt.putString(RelipetsConstantsRegistry.PET_SUMMON_STATE_KEY, this.summonState);
        nbt.putInt(HEALING_COOLDOWN, this.getHealingCooldown());
        nbt.putInt(NATURAL_HEALING_KEY, this.getNaturalHealing());
        nbt.putString(MOVEMENT_MODE_KEY, this.moveMode.name());

        if(this.petEntityData != null){
            nbt.put(RelipetsConstantsRegistry.PET_NBT_KEY, this.petEntityData.writeToNbt());
        }

        nbt.put(PET_INFO_KEY, this.petInfo.writeToNbt());

        return nbt;
    }

    public void addHighlight(MinecraftServer server) {

        LivingEntity entity =this.getPetEntityData().getEntity(server);
        if(entity != null){

            entity.setGlowing(true);

            Utils.setTimeout(()-> {
                entity.setGlowing(false);
            }, 10);

        }

    }



    public void applyNaturalHealing(MinecraftServer server) {

        LivingEntity pet = this.getPetEntityData().getEntity(server);
        if(pet == null) return;
        pet.setHealth(
                Math.min(pet.getMaxHealth(), pet.getHealth() + naturalHealing)
        );
    }

    private void updateTrackerIfNeeded(LivingEntity entity){
        if(this.isSummoned()){
            this.petEntityData.updateTracker(entity);
        }
    }

    public void renamePet(String name, MinecraftServer server) {
        if(!this.isSummoned()){
            this.getPetInfo().setPetName(name);
            System.out.println(this.getPetEntityData().getEntityNbt().toString());
        }else{
            LivingEntity entity =this.getPetEntityData().getEntity(server);

            if(entity != null){
                entity.setCustomName(Text.of(name));
            }
            this.getPetInfo().setPetName(name);
        }
    }

    public void summonForRelease(ServerWorld world, Vec3d pos, PlayerEntity player) {
        this.getPetEntityData().spawnEntityForRelease(world, pos, player, this);
    }

    public void applySlotContentChange(int inventorySlotIndex, MinecraftServer server) {
        if(!this.isSummoned()){
            PartSystem partSystem = this.tempPartSystem;

            partSystem.updateSystemBasedOnSlotIndex(inventorySlotIndex);
            NbtCompound serializedPartSystem = new NbtCompound();
            partSystem.writeToNbt(serializedPartSystem);
            this.getPetEntityData().getEntityNbt().put("part_system", serializedPartSystem);
        }

    }

    public void syncItemsWithPartSystem(MinecraftServer server){

        if(!this.isSummoned()){
            PartSystem partSystem = this.tempPartSystem;

            partSystem.applyInventoryChange();
            NbtCompound serializedPartSystem = new NbtCompound();
            partSystem.writeToNbt(serializedPartSystem);
            this.getPetEntityData().getEntityNbt().put("part_system", serializedPartSystem);
        }
    }

    //this is a temporary part system reference that will only be "valid" when the part management screen is open
    private PartSystem tempPartSystem;

    public PartSystem loadPartSystemIntoMemory(){
        NbtCompound partSystemNbt = this.getPetEntityData().getEntityNbt().getCompound("part_system");
        this.tempPartSystem = new PartSystem(partSystemNbt);
        return this.tempPartSystem;
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
