package net.daniel.relipets.entity.brain.sensor;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreOwnerSensor<C extends BaseCore> extends ExtendedSensor<C> {

    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleType.NEAREST_PLAYERS);

    private int maxDist = 30;

    public CoreOwnerSensor(){
    }

    public CoreOwnerSensor(int maxDist){
        this.maxDist = maxDist;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return RelipetsSensorTypes.CORE_OWNER_SENSOR.get();
    }

    @Override
    protected void sense(ServerWorld level, C baseCoreEntity) {

        List<PlayerEntity> playersNearby = (List<PlayerEntity>) baseCoreEntity.getWorld().getPlayers();

        for(PlayerEntity player : playersNearby){
            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);

            if(petOwnerSystem.getPetParty().getPetByEntityUUID(baseCoreEntity.getUuidAsString()) != null){
                //pet belongs to this pet owner
                BrainUtils.setMemory(baseCoreEntity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER, player);
                if(player.squaredDistanceTo(baseCoreEntity) <= this.maxDist * this.maxDist){
                    BrainUtils.setMemory(baseCoreEntity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER_NEARBY, true);
                    Relipets.LOGGER.debug("Set owner nearby to true");
                }
                Relipets.LOGGER.debug("CoreOwnerSensor found core owner");
                break;
            }

        }

    }
}
