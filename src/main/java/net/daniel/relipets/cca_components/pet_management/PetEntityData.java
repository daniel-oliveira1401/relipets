package net.daniel.relipets.cca_components.pet_management;

import com.google.common.collect.*;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.entity.cores.progression.StatsEnum;
import net.daniel.relipets.entity.cores.progression.UpgradableStats;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class PetEntityData implements ISerializable {

    public static final String ENTITY_NBT_KEY = "entity_nbt";
    public static final String ENTITY_TYPE_KEY = "entity_type";
    public static final String ENTITY_UUID_KEY = "entity_uuid";
    public static final String ENTITY_TRACKER_KEY = "entity_tracker";
    private static final String ENTITY_ID_KEY = "entity_id";

    @Getter
    NbtCompound entityNbt;

    @Getter
    LivingEntity entity;

    @Getter
    String entityType;
    @Getter
    String entityUUID;

    @Getter
    int entityId;

    @Getter
    PetEntityTracker tracker = new PetEntityTracker();

    public void loadEntityAndPerformAction(MinecraftServer server, Function<LivingEntity, Boolean> actionToPerform){

        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            Relipets.LOGGER.debug("Could not find world this entity was last seen at. World: " + this.getTracker().getDimension().toString());
            return;
        }


        ChunkPos lastKnownChunkPos = this.getTracker().getChunkPos();

        Relipets.LOGGER.debug("Loaded 3x3 area around entity last known pos");
        //load 3x3 area around the last know chunk
        for(int x = -1 ; x <= 1; x++){
            for(int z = -1 ; z <= 1; z++){
                world.setChunkForced(lastKnownChunkPos.x + x, lastKnownChunkPos.z + z, true);
            }
        }

        //unload the 3x3 area around the last know chunk after x ticks

        Utils.setTimeout(() -> {

            LivingEntity entity = (LivingEntity) world.getEntity(UUID.fromString(entityUUID));
            if(entity != null){
                Relipets.LOGGER.debug("Performing action");
                actionToPerform.apply(entity);
            }else{
                Relipets.LOGGER.debug("Could not find entity even after loading the area aroudn it.");
            }

            Relipets.LOGGER.debug("Unloading 3x3 area around entity last known pos");
            for(int x = -1 ; x <= 1; x++){
                for(int z = -1 ; z <= 1; z++){
                    world.setChunkForced(lastKnownChunkPos.x + x, lastKnownChunkPos.z + z, false);
                }
            }
        }, 60);

    }

    @Nullable
    public PetMetadataComponent getMetadata(World world){

        Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(world.getEntityById(this.entityId));

        if(petMetadataComponent.isPresent()){
            return petMetadataComponent.get();
        }

        return null;
    }

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

    public void saveEntityData(){
        cleanEntityBeforeSaving();

        this.entityType = EntityType.getId(entity.getType()).toString();
        this.entityNbt = entity.writeNbt(new NbtCompound());
        this.entityUUID = entity.getUuidAsString();
        this.entityId = entity.getId();
    }

    private void cleanEntityBeforeSaving() {
        if(this.entity != null){
            //clean glowing
            if(this.entity.isGlowing()){
                this.entity.setGlowing(false);
            }

            //clean velocity
            this.entity.setVelocity(0, 0, 0);

            //clean fire
            this.entity.setOnFire(false);


        }
    }

    public void spawnEntity(ServerWorld world, Vec3d pos, PlayerEntity player){
        Identifier entityTypeId = new Identifier(this.entityType);

        EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

        LivingEntity createdEntity = entityType.create(world);

        if(createdEntity == null) return;

        createdEntity.readNbt(entityNbt);

        if(createdEntity instanceof MobEntity mob) mob.setPersistent(); //idk if this actually works. Too hard to test

        createdEntity.setPosition(pos);
        createdEntity.setVelocity(0, 0, 0);
        createdEntity.setOnFire(false);
        createdEntity.setGlowing(false);
        world.getServer().execute(()->{
            this.applyStatModifiers(createdEntity);
            world.spawnEntity(createdEntity);
        });

        this.setEntity(createdEntity);

        this.setOwner(player);
    }

    //TODO: fix this code. It does not work if the entity is in an unloaded chunk when attempting to bind them
    public void bindEntity(ServerWorld world, PlayerEntity player){
        //search in current world
        LivingEntity entity = (LivingEntity) world.getEntity(UUID.fromString(this.getEntityUUID()));

        if(entity != null){
            this.entity = entity;
            this.setOwner(player);
            this.applyStatModifiers(this.entity);
            this.entityId = this.entity.getId();

            Relipets.LOGGER.debug("Bound entity successfully");
            System.out.println("Bound entity successfully");
        }else{
            //search in the world of the tracker
            ServerWorld trackerWorld = this.getTracker().getWorld(world.getServer());

            if(trackerWorld != null){
                entity = (LivingEntity) trackerWorld.getEntity(UUID.fromString(this.getEntityUUID()));
            }


            if(entity != null){

                this.entity = entity;
                this.setOwner(player);
                this.applyStatModifiers(this.entity);
                this.entityId = this.entity.getId();
                System.out.println("Bound entity successfully");

            }else{
                this.loadEntityAndPerformAction(world.getServer(), (entityLoaded)-> {

                    this.entity = entityLoaded;
                    this.setOwner(player);
                    this.applyStatModifiers(this.entity);
                    this.entityId = this.entity.getId();
                    Relipets.LOGGER.debug("Bound entity successfully after loading it");
                    System.out.println("Bound entity successfully");

                    return true;
                });

            }

        }
    }

    public void setOwner(PlayerEntity player){
        if(this.entity != null){
            PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(this.entity);
            petMetadata.setPlayerUUID(player.getUuidAsString());
            CardinalComponentsRegistry.PET_METADATA_KEY.sync(this.entity);
        }else{
            Relipets.LOGGER.debug("There must be an entity in order to set the owner");
        }
    }

    //TODO: fix this code. This does not cover all the possible recall scenarios
    public boolean recallEntity(ServerWorld currentWorld, Function<Boolean, Boolean> setRecalledState){

        saveEntityData();

        ServerWorld world = this.getTracker().getWorld(currentWorld.getServer());

        if(world == null){
            Relipets.LOGGER.debug("Could not find world: " + this.getTracker().getDimension().toString());
            return false;
        }

        //try retrieving from the world the entity was last seen at
        LivingEntity entityFound = (LivingEntity) world.getEntity(UUID.fromString(this.entityUUID));

        if(entityFound != null){
            cleanEntityBeforeSaving();
            removeEntity(entityFound);
            this.entity = null;
            return true;
        }else{

            //try retrieving from the current world
            entityFound = (LivingEntity) currentWorld.getEntity(UUID.fromString(this.entityUUID));

            if(entityFound == null){
                //try loading the last place they were seen at

                loadEntityAndPerformAction(currentWorld.getServer(),(entityLoaded)->{
                    cleanEntityBeforeSaving();
                    removeEntity(entityLoaded);
                    this.entity = null;
                    Relipets.LOGGER.debug("Loaded entity and removed it");
                    setRecalledState.apply(true);
                    return true;
                });
            }


        }

        return false;
    }

    private void removeEntity(LivingEntity entity){

        entity.remove(Entity.RemovalReason.DISCARDED);

    }

    public void updateTracker(){
        if(this.entity != null && this.entity.isAlive() && !this.entity.isRemoved()){
            tracker.setPosition(this.entity.getBlockPos());
            tracker.setDimension(this.entity.getWorld().getRegistryKey().getValue());

        }
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.entityNbt = nbt.getCompound(ENTITY_NBT_KEY);
        this.entityType = nbt.getString(ENTITY_TYPE_KEY);
        this.entityUUID = nbt.getString(ENTITY_UUID_KEY);
        this.entityId = nbt.getInt(ENTITY_ID_KEY);
        this.tracker = new PetEntityTracker();
        if(nbt.contains(ENTITY_TRACKER_KEY))
            this.tracker.readFromNbt(nbt.getCompound(ENTITY_TRACKER_KEY));
    }

    @Override
    public NbtCompound writeToNbt() {

        NbtCompound nbt = new NbtCompound();
        nbt.putString(ENTITY_TYPE_KEY, this.entityType);
        nbt.put(ENTITY_NBT_KEY, entityNbt);
        nbt.putString(ENTITY_UUID_KEY, entityUUID);
        nbt.put(ENTITY_TRACKER_KEY, this.tracker.writeToNbt());
        nbt.putInt(ENTITY_ID_KEY, this.entityId);

        return nbt;
    }

    public void applyStatModifiers(LivingEntity entity) {
        Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(entity);

        if(petMetadataComponent.isPresent()) {
            PetMetadataComponent petMetadata = petMetadataComponent.get();
            UpgradableStats stats = petMetadata.getStatUpgrades();

            Multimap<EntityAttribute, EntityAttributeModifier> statModifiersMap = ArrayListMultimap.create();

            double healthBuffValue = UpgradableStats
                    .getStatScalingByCategory(
                            StatsEnum.HEALTH,
                            UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.HEALTH);

            EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                    UUID.fromString("a09b45d1-ca1a-4ff6-8f67-e7ad1415f664"),
                    "relipets_health_modifier",
                    healthBuffValue,
                    EntityAttributeModifier.Operation.ADDITION);

            statModifiersMap.put(EntityAttributes.GENERIC_MAX_HEALTH, healthModifier);
            entity.getAttributes().addTemporaryModifiers(statModifiersMap);
            CardinalComponentsRegistry.PET_METADATA_KEY.sync(entity);
        }
    }

    @Getter
    @Setter
    public static class PetEntityTracker implements ISerializable {

        public static final String DIMENSION_KEY = "tracker_dimension";
        public static final String POSITION_KEY = "tracker_position";

        Identifier dimension;
        BlockPos position;

        public ChunkPos getChunkPos(){
            return new ChunkPos(
                    this.position.getX()/16,
                    this.position.getZ()/16
            );
        }

        @Nullable
        public ServerWorld getWorld(MinecraftServer server){

            return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, this.dimension));

        }

        @Override
        public void readFromNbt(NbtCompound nbt) {
            if(nbt.contains(DIMENSION_KEY)){
                this.dimension = new Identifier(nbt.getString(DIMENSION_KEY));
            }

            if(nbt.contains(POSITION_KEY)){
                this.position = Utils.deserializeBlockPos(nbt.getString(POSITION_KEY));
            }
        }

        @Override
        public NbtCompound writeToNbt() {

            NbtCompound nbt = new NbtCompound();

            if(this.dimension != null){
                nbt.putString(DIMENSION_KEY, dimension.toString());
            }
            if(this.position != null){
                nbt.putString(POSITION_KEY, Utils.serializeBlockPos(this.position));

            }

            return nbt;
        }


    }

}
