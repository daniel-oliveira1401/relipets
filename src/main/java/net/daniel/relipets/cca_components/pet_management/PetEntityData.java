package net.daniel.relipets.cca_components.pet_management;

import com.google.common.collect.*;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.UpgradableStats;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.SetTimeoutManager;
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
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Getter
public class PetEntityData implements ISerializable {

    public static final String ENTITY_NBT_KEY = "entity_nbt";
    public static final String ENTITY_TYPE_KEY = "entity_type";
    public static final String ENTITY_UUID_KEY = "entity_uuid";
    public static final String ENTITY_TRACKER_KEY = "entity_tracker";
    private static final String ENTITY_ID_KEY = "entity_id";

    NbtCompound entityNbt;

    LivingEntity entity;

    String entityType;
    String entityUUID;

    int entityId;

    PetEntityTracker tracker = new PetEntityTracker();

    public void loadEntityAndPerformAction(MinecraftServer server, Function<LivingEntity, Boolean> actionToPerform){

        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            System.out.println("Could not find world this entity was last seen at. World: " + this.getTracker().getDimension().toString());
            return;
        }


        ChunkPos lastKnownChunkPos = this.getTracker().getChunkPos();

        System.out.println("Loaded 3x3 area around entity last known pos");
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
                System.out.println("Performing action");
                actionToPerform.apply(entity);
            }else{
                System.out.println("Could not find entity even after loading the area aroudn it.");
            }

            System.out.println("Unloading 3x3 area around entity last known pos");
            for(int x = -1 ; x <= 1; x++){
                for(int z = -1 ; z <= 1; z++){
                    world.setChunkForced(lastKnownChunkPos.x + x, lastKnownChunkPos.z + z, false);
                }
            }
        }, Utils.secondToTick(3));

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

    public void spawnEntity(ServerWorld world, Vec3d pos, PlayerEntity player, PetData petData){
        world.getServer().execute(()-> {
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
            createdEntity.fallDistance = 0;

            world.spawnEntity(createdEntity);
            Utils.message("Summoned " + createdEntity.getDisplayName().getString() + ".", player);
            SetTimeoutManager.setTimeout(()-> {
                this.applyStatModifiers(createdEntity, petData);
            }, Utils.secondToTick(1));

            this.setEntity(createdEntity);

            this.setOwner(player);
        });

    }

    private void applyBinding(PlayerEntity player, LivingEntity entity, PetData petData){
        this.entity = entity;
        this.setOwner(player);
        this.applyStatModifiers(this.entity, petData);
        this.entityId = this.entity.getId();
    }

    public void bindEntity(ServerWorld world, PlayerEntity player, PetData petData){
        //SetTimeoutManager.setTimeout(()-> {
            //search in current world
            LivingEntity entity = (LivingEntity) world.getEntity(UUID.fromString(this.getEntityUUID()));

            if(entity != null){

                applyBinding(player, entity, petData);

                Relipets.LOGGER.debug("Bound entity successfully");
                System.out.println("Bound entity successfully from player world");
            }else{
                //search in the world of the tracker
                ServerWorld trackerWorld = this.getTracker().getWorld(world.getServer());

                if(trackerWorld != null){
                    entity = (LivingEntity) trackerWorld.getEntity(UUID.fromString(this.getEntityUUID()));
                }


                if(entity != null){

                    applyBinding(player, entity, petData);
                    System.out.println("Bound entity successfully from tracker");

                }else{
                    this.loadEntityAndPerformAction(world.getServer(), (entityLoaded)-> {

                        applyBinding(player, entityLoaded, petData);
                        Relipets.LOGGER.debug("Bound entity successfully after loading it");
                        System.out.println("Bound entity successfully after loading chunks");

                        return true;
                    });

                }

            }
        //}, Utils.secondToTick(2));

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

    public boolean recallEntity(ServerWorld currentWorld, PlayerEntity player, Function<Boolean, Boolean> setRecalledState){

        saveEntityData();

        ServerWorld trackerWorld = this.getTracker().getWorld(currentWorld.getServer());

        if(trackerWorld == null){
            Relipets.LOGGER.debug("Could not find world: " + this.getTracker().getDimension().toString());
            return false;
        }

        //try retrieving from the world the entity was last seen at
        LivingEntity entityFound = (LivingEntity) trackerWorld.getEntity(UUID.fromString(this.entityUUID));

        if(entityFound != null){
            Utils.message("Recalled " + this.getEntity().getDisplayName().getString() + ".", player);
            cleanEntityBeforeSaving();
            removeEntity(entityFound);
            this.entity = null;

            return true;
        }else{

            //try retrieving from the current world
            entityFound = (LivingEntity) currentWorld.getEntity(UUID.fromString(this.entityUUID));

            if(entityFound != null){
                Utils.message("Recalled " + this.getEntity().getDisplayName().getString() + ".", player);
                cleanEntityBeforeSaving();
                removeEntity(entityFound);
                this.entity = null;
                return true;
            }else {
                //try loading the last place they were seen at
                String entityName = this.getEntity().getDisplayName().getString();
                Utils.message(entityName + " was last seen at " +
                        trackerWorld.getDimensionKey().getValue().toString() +
                        " " + tracker.getPosition().toShortString() +
                        ". Trying to recall them from there.", player);

                loadEntityAndPerformAction(currentWorld.getServer(),(entityLoaded)->{
                    Utils.message("Recalled " + this.getEntity().getDisplayName().getString() + ".", player);
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

    public void applyStatModifiers(LivingEntity entity, PetData pet) {
        Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(entity);

        if(petMetadataComponent.isPresent()) {
            PetMetadataComponent petMetadata = petMetadataComponent.get();
            UpgradableStats stats = petMetadata.getStatUpgrades();

            Multimap<EntityAttribute, EntityAttributeModifier> statModifiersMap = ArrayListMultimap.create();

            if(this.entityHasStat(entity.getWorld(), StatsEnum.HEALTH)){

                //====== health ===
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
                //==========
            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ATTACK)){

                //====== Attack ===
                double attackBuff = UpgradableStats
                        .getStatScalingByCategory(
                                StatsEnum.ATTACK,
                                UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ATTACK);

                EntityAttributeModifier attackModifier = new EntityAttributeModifier(
                        UUID.fromString("2f7b00e9-8f9f-46e4-84e8-f2e765222df0"),
                        "relipets_attack_modifier",
                        attackBuff,
                        EntityAttributeModifier.Operation.ADDITION);

                statModifiersMap.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, attackModifier);
            }

            //==========

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ARMOR)) {

                //====== Armor ===
                double armorBuff = UpgradableStats
                        .getStatScalingByCategory(
                                StatsEnum.ARMOR,
                                UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ARMOR);

                EntityAttributeModifier armorModifier = new EntityAttributeModifier(
                        UUID.fromString("6b01e11d-0e74-4840-b8e7-d31be3c08be1"),
                        "relipets_armor_modifier",
                        armorBuff,
                        EntityAttributeModifier.Operation.ADDITION);

                statModifiersMap.put(EntityAttributes.GENERIC_ARMOR, armorModifier);

                //==========

            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ARMOR_TOUGHNESS)) {
                //====== Armor toughness ===
                double armorToughnessBuff = UpgradableStats
                        .getStatScalingByCategory(
                                StatsEnum.ARMOR_TOUGHNESS,
                                UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ARMOR_TOUGHNESS);

                EntityAttributeModifier armorToughnessModifier = new EntityAttributeModifier(
                        UUID.fromString("d2017fa1-9d52-4afd-8dc2-ecef7a6cd220"),
                        "relipets_armor_tough_modifier",
                        armorToughnessBuff,
                        EntityAttributeModifier.Operation.ADDITION);

                statModifiersMap.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, armorToughnessModifier);

                //==========
            }


            entity.getAttributes().addTemporaryModifiers(statModifiersMap);

            //===== Health regen
            double healthRegenBuffValue = UpgradableStats
                    .getStatScalingByCategory(
                            StatsEnum.HEALTH_REGEN,
                            UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.HEALTH_REGEN);

            healthRegenBuffValue += PetData.BASE_NATURAL_HEALING;

            pet.setNaturalHealing(
                    (int) healthRegenBuffValue
            );

            CardinalComponentsRegistry.PET_METADATA_KEY.sync(entity);
        }
    }

    public boolean entityHasStat(World world, StatsEnum stat) {
        Entity entity = world.getEntityById(this.getEntityId());

        if(entity instanceof LivingEntity living){
            return UpgradableStats.entityHasStat(living, stat);
        }

        return false;
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
