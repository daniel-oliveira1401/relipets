package net.daniel.relipets.entity.brain.sensor;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * This sensor is used to add the behavior of feeling like doing something at random. It will randomly
 * try to set the memory associated with it to true.
 */
public class FeelLikeDoingSomethingSensor<C extends BaseCore> extends ExtendedSensor<C> {
    private MemoryModuleType<String> memory;
    private float chance = 0.5f;

    public FeelLikeDoingSomethingSensor(){
    }

    @Override
    public ExtendedSensor<C> afterScanning(Consumer<C> callback) {
        return this;
    }

    public FeelLikeDoingSomethingSensor<C> affectsMemory(MemoryModuleType<String> memory){
        this.memory = memory;
        return this;
    }

    public FeelLikeDoingSomethingSensor<C> withChance(float chance){
        this.chance = chance;

        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return List.of(this.memory);
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return RelipetsSensorTypes.FEEL_LIKE_DOING_SOMETHING.get();
    }

    @Override
    public void sense(ServerWorld level, BaseCore entity) {
        if(Math.random() <= this.chance){
            BrainUtils.setMemory(entity.getBrain(), this.memory, "yes you should");
        }
    }
}
