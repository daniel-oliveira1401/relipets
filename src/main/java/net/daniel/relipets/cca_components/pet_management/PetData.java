package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

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

    private boolean needsEntityBinding(){

        return this.petEntityData != null && //has entity data
                this.petEntityData.isValid() && //the data is valid
                this.summonState.equals(SUMMONED) && //the pet is summoned somewhere in the world
                this.petEntityData.entity == null; //but it is not bound to the entity data

    }

    public void tick(ServerWorld world, PlayerEntity player){

        handleEntityBindingIfNeeded(world, player);

        updateTrackerIfNeeded(world);

        if(this.isHealing()){
            this.healingCooldown = Math.max(this.healingCooldown - 1, 0);

            if(this.healingCooldown == 0){
                this.summonState = RECALLED;
                Relipets.LOGGER.debug("Pet is healed!");
            }
        }

    }

    //TODO: split info into volatile and non-volatile. Update only the volatile info
    public void updateVolatilePetInfoIfPossible(){
        if(this.isSummoned()){
            this.getPetInfo().setPetName(this.getPetEntityData().getEntity().getDisplayName().getString());
            this.getPetInfo().setMaxHealth((int) this.getPetEntityData().getEntity().getMaxHealth());
            this.getPetInfo().setCurrentHealth((int) this.getPetEntityData().getEntity().getHealth());

        }
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
            this.getPetEntityData().bindEntity(world, player);
            pendingEntityBind = false;
        }
    }

    public boolean isSummoned(){
        boolean summonStateSummoned = this.summonState.equals(SUMMONED);

        boolean validEntityData = this.getPetEntityData().isValid();

        boolean hasEntity = this.getPetEntityData().entity != null;

        return summonStateSummoned && validEntityData && hasEntity;
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
    }

    public void recall(ServerWorld world){

        boolean recalled = this.getPetEntityData().recallEntity(world, (e)-> {
            this.summonState = RECALLED;
            return true;
        });

        if(recalled)
            this.summonState = RECALLED;

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
            this.getPetEntityData().getEntity().setGlowing(false);
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

    @Getter
    @Setter
    public static class PetInfo implements ISerializable{

        public static final String NAME_KEY = "pet_info_name";
        public static final String CURRENT_HEALTH_KEY = "pet_info_current_health";
        public static final String MAX_HEALTH_KEY = "pet_info_max_health";

        private String petName = "";
        private int currentHealth = 0;
        private int maxHealth = 0;

        @Override
        public void readFromNbt(NbtCompound nbt) {
            if(nbt.contains(NAME_KEY)){
                this.setPetName(nbt.getString(NAME_KEY));
            }

            if(nbt.contains(CURRENT_HEALTH_KEY))
                this.setCurrentHealth(nbt.getInt(CURRENT_HEALTH_KEY));

            if(nbt.contains(MAX_HEALTH_KEY))
                this.setMaxHealth(nbt.getInt(MAX_HEALTH_KEY));
        }

        @Override
        public NbtCompound writeToNbt() {

            NbtCompound nbt = new NbtCompound();
            nbt.putString(NAME_KEY, this.getPetName());
            nbt.putInt(CURRENT_HEALTH_KEY, this.getCurrentHealth());
            nbt.putInt(MAX_HEALTH_KEY, this.getMaxHealth());

            return nbt;
        }

    }

}
