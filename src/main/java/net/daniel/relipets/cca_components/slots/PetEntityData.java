package net.daniel.relipets.cca_components.slots;

import net.daniel.relipets.cca_components.ISerializable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class PetEntityData implements ISerializable {

    public static final String ENTITY_NBT_KEY = "entity_nbt";
    public static final String ENTITY_TYPE_KEY = "entity_type";
    public static final String ENTITY_UUID_KEY = "entity_uuid";

    NbtCompound entityNbt;

    LivingEntity entity;

    String entityType;

    String entityUUID;

    //Valid: has entity data
    public boolean isValid(){
        boolean validEntityType = entityType != null && !entityType.isEmpty();
        boolean validNbt = !entityNbt.isEmpty();
        boolean validUUID = entityUUID != null && !entityUUID.isEmpty();

        return validEntityType && validNbt && validUUID;
    }

    public void setEntity(LivingEntity entity){
        this.entity = entity;
        saveEntityData();
    }

    private void saveEntityData(){
        this.entityType = EntityType.getId(entity.getType()).toString();
        this.entityNbt = entity.writeNbt(new NbtCompound());
        this.entityUUID = entity.getUuidAsString();
    }

    public LivingEntity getEntity(){
        return this.entity;
    }

    public void spawnEntity(ServerWorld world, Vec3d pos){
        Identifier entityTypeId = new Identifier(this.entityType);

        EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

        LivingEntity createdEntity = entityType.create(world);
        createdEntity.readNbt(entityNbt);

        createdEntity.setPosition(pos);

        world.spawnEntity(createdEntity);

        this.setEntity(createdEntity);
    }

    public void recallEntity(){

        saveEntityData();

        entity.remove(Entity.RemovalReason.DISCARDED);

        this.entity = null;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.entityNbt = nbt.getCompound(ENTITY_NBT_KEY);
        this.entityType = nbt.getString(ENTITY_TYPE_KEY);
        this.entityUUID = nbt.getString(ENTITY_UUID_KEY);
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();
        nbt.putString(ENTITY_TYPE_KEY, this.entityType);
        nbt.put(ENTITY_NBT_KEY, entityNbt);
        nbt.putString(ENTITY_UUID_KEY, entityUUID);

        return nbt;
    }
}
