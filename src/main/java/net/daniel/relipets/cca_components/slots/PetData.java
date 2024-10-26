package net.daniel.relipets.cca_components.slots;

import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class PetData implements ISerializable {

    public static final String SUMMONED = "summoned";
    public static final String RECALLED = "recalled";
    public static final String HEALING = "healing";

    public PetEntityData petEntityData;

    String summonState = RECALLED;

    public int petLevel = 0;

    public boolean needsBinding(){

        return this.petEntityData != null && //has entity data
                this.petEntityData.isValid() && //the data is valid
                this.isSummoned() && //the pet is summoned somewhere in the world
                this.petEntityData.entity == null; //but it is not bound to the entity data

    }

    public void bind(LivingEntity entity){
        this.petEntityData.entity = entity;
    }

    public boolean isSummoned(){
        boolean summonStateSummoned = this.summonState.equals(SUMMONED);

        boolean validEntityData = this.petEntityData.isValid();

        return summonStateSummoned && validEntityData;
    }

    public boolean isRecalled(){
        boolean summonStateRecalled = this.summonState.equals(RECALLED);

        boolean validEntityData = this.petEntityData.isValid();

        return summonStateRecalled && validEntityData;
    }

    public void summon(ServerWorld world, Vec3d pos){
        this.summonState = SUMMONED;
        this.petEntityData.spawnEntity(world, pos);
    }

    public void recall(){
        this.summonState = RECALLED;
        this.petEntityData.recallEntity();
    }

    public void fillFromEntity(LivingEntity entity){
        this.summonState = RECALLED;
        this.petLevel = 0;
        this.petEntityData = new PetEntityData();
        this.petEntityData.setEntity(entity);
        this.petEntityData.recallEntity();
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
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
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();

        nbt.putString(RelipetsConstantsRegistry.PET_SUMMON_STATE_KEY, this.summonState);
        nbt.putInt(RelipetsConstantsRegistry.PET_LEVEL_KEY, this.petLevel);

        if(this.petEntityData != null){
            nbt.put(RelipetsConstantsRegistry.PET_NBT_KEY, this.petEntityData.writeToNbt());

        }

        return nbt;
    }
}
