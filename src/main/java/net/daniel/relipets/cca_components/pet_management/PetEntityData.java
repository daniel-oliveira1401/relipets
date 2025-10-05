package net.daniel.relipets.cca_components.pet_management;

import com.google.common.collect.*;
import lombok.Getter;
import lombok.Setter;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.UpgradableStats;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.YellowCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.SetTimeoutManager;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@Getter
public class PetEntityData implements ISerializable {

    public static final String ENTITY_NBT_KEY = "entity_nbt";
    public static final String ENTITY_TYPE_KEY = "entity_type";
    public static final String ENTITY_UUID_KEY = "entity_uuid";
    public static final String ENTITY_TRACKER_KEY = "entity_tracker";
    private static final String ENTITY_ID_KEY = "entity_id";

    NbtCompound entityNbt;

    String entityType;
    String entityUUID;

    int entityId;

    PetEntityTracker tracker = new PetEntityTracker();

    @Nullable
    public LivingEntity getEntity(MinecraftServer server){
        
        ServerWorld world = tracker.getWorld(server);
        
        if(world != null){
            return (LivingEntity) world.getEntity(UUID.fromString(this.entityUUID));
        }
        
        return null;
    }
    
    public void loadEntityAndPerformAction(PetParty party, MinecraftServer server, Function<LivingEntity, Boolean> actionToPerform){

        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            System.out.println("Could not find world this entity was last seen at. World: " + this.getTracker().getDimension().toString());
            return;
        }

        ChunkPos lastKnownChunkPos = this.getTracker().getChunkPos();

        //unload the 3x3 area around the last know chunk after x ticks
        party.getChunkLoadManager().addRequest(
                new ChunkLoadRequest(
                        world,
                        ChunkLoadRequest.chunkAreaAroundCenterPoint(lastKnownChunkPos.x, lastKnownChunkPos.z, 3),
                        ()-> {
                            LivingEntity entity = (LivingEntity) world.getEntity(UUID.fromString(entityUUID));
                            if(entity != null){
                                System.out.println("Performing action");
                                actionToPerform.apply(entity);
                                return true;
                            }

                            System.out.println("Could not find entity even after loading the area around it.");

                            return false;

                        }
                )
        );

    }

    
//    @Nullable
//    public PetMetadataComponent getMetadata(World world){
//
//        Optional<PetMetadataComponent> petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.maybeGet(world.getEntityById(this.entityId));
//
//        if(petMetadataComponent.isPresent()){
//            return petMetadataComponent.get();
//        }
//
//        return null;
//    }

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

    public void saveEntityData(MinecraftServer server){
        LivingEntity entity = this.getEntity(server);
        if(entity != null){
            this.internalSaveEntityData(entity);
        }
    }

    public void saveEntityData(LivingEntity entity){
        if(entity != null){
            internalSaveEntityData(entity);
        }
    }

    private void internalSaveEntityData(LivingEntity entity){
        cleanEntityBeforeSaving(entity);
        this.tracker.updateFromEntity((ServerWorld) entity.getWorld(), entity);
        this.entityType = EntityType.getId(entity.getType()).toString();
        this.entityNbt = entity.writeNbt(new NbtCompound());
        this.entityUUID = entity.getUuidAsString();
        this.entityId = entity.getId();
        applyEntityDataPostProcessing(entity);
    }

    private void applyEntityDataPostProcessing(LivingEntity entity) {

        //add more edge cases here for weird mobs

        if(entity instanceof CreeperEntity){
            this.entityNbt.putBoolean("ignited", false);
        }
    }

    private void cleanEntityBeforeSaving(LivingEntity entity) {
        if(entity != null){
            //clean glowing
            if(entity.isGlowing()){
                entity.setGlowing(false);
            }

            //clean velocity
            entity.setVelocity(0, 0, 0);

            //clean fire
            entity.setOnFire(false);
            entity.setFireTicks(0);
            entity.wasOnFire = false;

        }
    }

    @Nullable
    private LivingEntity createAndInitializeEntity(PetData petData, Vec3d pos, ServerWorld world){
        Identifier entityTypeId = Identifier.of(this.entityType);

        EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

        LivingEntity createdEntity = entityType.create(world);

        if(createdEntity == null) return null;

        createdEntity.readNbt(entityNbt);

        if(createdEntity instanceof MobEntity mob) mob.setPersistent(); //idk if this actually works. Too hard to test

        createdEntity.setPosition(pos);
        createdEntity.setVelocity(0, 0, 0);
        createdEntity.setOnFire(false);
        createdEntity.setGlowing(false);
        createdEntity.fallDistance = 0;
        createdEntity.setCustomName(Text.of(petData.getPetInfo().getPetName()));
        UUID uuid = UUID.randomUUID();
        createdEntity.setUuid(uuid);
        this.entityUUID = uuid.toString();

        return createdEntity;
    }

    public void spawnEntity(ServerWorld world, Vec3d pos, PlayerEntity player, PetData petData){
        world.getServer().execute(()-> {

            LivingEntity createdEntity = this.createAndInitializeEntity(petData, pos, world);

            if(createdEntity == null) return;

            world.spawnEntity(createdEntity);
            this.tracker.updateFromEntity(world, createdEntity);
            Utils.message("Summoned " + createdEntity.getDisplayName().getString() + ".", player);
            SetTimeoutManager.setTimeout(()-> {
                this.applyStatModifiers(createdEntity, petData);
            }, Utils.secondToTick(1));


            this.setOwner(player, createdEntity);
            petData.followDistance = (int) Math.max(PetData.BASE_FOLLOW_DISTANCE, Math.min(createdEntity.getBoundingBox().getAverageSideLength() * PetData.BASE_FOLLOW_DISTANCE, 40));
            Utils.log("Follow distance for " + petData.getPetInfo().getPetName() + ": " + petData.followDistance);
            petData.teleportDistance = (int) Math.min(60, petData.followDistance + PetData.TP_DISTANCE_DELTA);
            Utils.log("Teleport distance for " + petData.getPetInfo().getPetName() + ": " + petData.teleportDistance);
        });

    }

    public void setOwner(PlayerEntity player, LivingEntity entity){
        
        PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(entity);
        petMetadata.setPlayerUUID(player.getUuidAsString());
        CardinalComponentsRegistry.PET_METADATA_KEY.sync(entity);
        
    }

    public void recallSync(MinecraftServer server, PetData petData, LivingEntity entityLoaded, PlayerEntity player){
        Utils.message("Recalled " + petData.getPetInfo().getPetName() + ".", player);
        saveEntityData(server);
        removeEntity(entityLoaded);
        Utils.log("Loaded entity and removed it");
    }

    public boolean recallEntity(PetParty party, PetData petData, ServerWorld currentWorld, PlayerEntity player, Function<Boolean, Boolean> setRecalledState){

        ServerWorld trackerWorld = this.getTracker().getWorld(currentWorld.getServer());

        if(trackerWorld == null){
            Utils.log("Could not find world: " + this.getTracker().getDimension().toString());
            return false;
        }
        
        //try loading the last place they were seen at
        String entityName = petData.getPetInfo().getPetName();
        Utils.message(entityName + " was last seen at " +
                trackerWorld.getDimensionKey().getValue().toString() +
                " " + tracker.getPosition().toShortString() +
                ". Trying to recall them from there.", player);

        loadEntityAndPerformAction(party, trackerWorld.getServer(),(entityLoaded)->{
            recallSync(player.getServer(), petData, entityLoaded, player);
            setRecalledState.apply(true);
            return true;
        });
        
        return false;
    }

    private void removeEntity(LivingEntity entity){

        entity.remove(Entity.RemovalReason.CHANGED_DIMENSION);

    }

    public void updateTracker(LivingEntity entity){
        
        if(entity != null){

            tracker.updateFromEntity((ServerWorld) entity.getWorld(), entity);
            
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
            if(stats == null) return;

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
                        UUID.fromString("30190be1-5f46-4706-8a9d-e46458edb3c1"),
                        "relipets_attack_modifier_2",
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

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ABILITY_RANGE)) {
                //====== Armor toughness ===
                float abilityRange = UpgradableStats.getStatScalingByCategory(
                        StatsEnum.ABILITY_RANGE,
                        UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ABILITY_RANGE);

                if(entity instanceof BaseCore core){
                    core.getAbilityStats().setAbilityRange(abilityRange + 1);
                }

                //==========
            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ABILITY_STRENGTH)) {
                //====== Strength ===
                float abilityRange = UpgradableStats.getStatScalingByCategory(
                        StatsEnum.ABILITY_STRENGTH,
                        UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ABILITY_STRENGTH);

                if(entity instanceof BaseCore core){
                    core.getAbilityStats().setAbilityStrength(abilityRange + 1);
                }

                //==========
            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.ABILITY_DURATION)) {
                //====== Duration ===
                float abilityRange = UpgradableStats.getStatScalingByCategory(
                        StatsEnum.ABILITY_DURATION,
                        UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.ABILITY_DURATION);

                if(entity instanceof BaseCore core){
                    core.getAbilityStats().setAbilityDuration(abilityRange + 1);
                }

                //==========
            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.MINIMUM_BOOST_SPEED)) {
                //====== Duration ===
                float abilityRange = UpgradableStats.getStatScalingByCategory(
                        StatsEnum.MINIMUM_BOOST_SPEED,
                        UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.MINIMUM_BOOST_SPEED);

                if(entity instanceof YellowCore core){
                    float speed = abilityRange + YellowCore.DEFAULT_MIN_BOOST_SPEED;
                    core.getYellowCoreStats().setMinBoostSpeed(speed);
                    //core.getDataTracker().set(YellowCore.MIN_BOOST_SPEED, speed);
                }

                //==========
            }

            if(this.entityHasStat(entity.getWorld(), StatsEnum.MAXIMUM_BOOST_SPEED)) {
                //====== Duration ===
                float abilityRange = UpgradableStats.getStatScalingByCategory(
                        StatsEnum.MAXIMUM_BOOST_SPEED,
                        UpgradableStats.getEntityCategory(entity)) * stats.getStatValue(StatsEnum.MAXIMUM_BOOST_SPEED);

                if(entity instanceof YellowCore core){
                    core.getYellowCoreStats().setMaxBoostSpeed(abilityRange + YellowCore.DEFAULT_MAX_BOOST_SPEED);
                }

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

    public void requestTeleportTo(PetParty petParty, MinecraftServer server, BlockPos blockPos) {
        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            Utils.log("Could not proceed with teleport request due to world not being found");
            return;
        }

        ChunkPos chunkPos = this.getTracker().getChunkPos();

        petParty.getChunkLoadManager().addRequest(
                new ChunkLoadRequest(
                        world,
                        ChunkLoadRequest.chunkAreaAroundCenterPoint(chunkPos.x, chunkPos.z, 3),
                        ()-> {
                            LivingEntity pet = (LivingEntity) world.getEntity(UUID.fromString(this.entityUUID));

                            if(pet != null){

                                BrainUtils.clearMemory(pet, MemoryModuleType.WALK_TARGET);
                                if(pet instanceof MobEntity entity)
                                    entity.getNavigation().stop();

                                Vec3d targetPos = blockPos.up().toCenterPos();

                                pet.requestTeleport(
                                        targetPos.getX(),
                                        targetPos.getY(),
                                        targetPos.getZ()
                                );

                                return true;
                            }

                            return false;
                        }
                )
        );
    }

    public void spawnEntityForRelease(ServerWorld world, Vec3d pos, PlayerEntity player, PetData petData) {
        world.getServer().execute(()-> {
            Identifier entityTypeId = Identifier.of(this.entityType);

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
            createdEntity.setCustomName(Text.of(petData.getPetInfo().getPetName()));
            UUID uuid = UUID.randomUUID();
            createdEntity.setUuid(uuid);
            this.entityUUID = uuid.toString();

            world.spawnEntity(createdEntity);

            SetTimeoutManager.setTimeout(()-> {
                this.applyStatModifiers(createdEntity, petData);
                PetMetadataComponent petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.get(createdEntity);
                petMetadataComponent.clearPlayerUUID();
            }, Utils.secondToTick(1));

        });
    }

    public void loadAreaAroundEntity(MinecraftServer server, PetParty party) {
        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            System.out.println("Could not find world this entity was last seen at. World: " + this.getTracker().getDimension().toString());
            return;
        }

        ChunkPos lastKnownChunkPos = this.getTracker().getChunkPos();

        party.getChunkLoadManager().addRequest(
                new ChunkLoadRequest(
                        world,
                        ChunkLoadRequest.chunkAreaAroundCenterPoint(lastKnownChunkPos.x, lastKnownChunkPos.z, 3),
                        ()-> {
                            LivingEntity entity = (LivingEntity) world.getEntity(UUID.fromString(entityUUID));

                            if(entity != null){
                                this.saveEntityData(world.getServer());
                                party.pushChangesToClient();
                                return true;
                            }

                            return false;

                        }
                )
        );

    }

    public void unloadAreaAroundEntity(MinecraftServer server) {
        ServerWorld world = this.getTracker().getWorld(server);

        if(world == null){
            System.out.println("Could not find world this entity was last seen at. World: " + this.getTracker().getDimension().toString());
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
                this.dimension = Identifier.of(nbt.getString(DIMENSION_KEY));
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

        @Override
        public String toString() {
            return String.format("%s at X:%s, Y:%s, Z:%s",
                    this.dimension.toString(),
                    this.position.getX(),
                    this.position.getY(),
                    this.position.getZ());
        }

        public void updateFromEntity(ServerWorld world, LivingEntity entity) {
            this.setDimension(world.getRegistryKey().getValue());
            this.setPosition(entity.getBlockPos());
        }
    }

}
