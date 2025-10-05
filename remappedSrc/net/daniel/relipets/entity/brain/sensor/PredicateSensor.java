package net.daniel.relipets.entity.brain.sensor;

import lombok.Getter;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PredicateSensor extends ExtendedSensor<BaseCore> {

    protected Predicate<BaseCore> predicate;
    protected List<MemoryPair<?>> memories;

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return this.memories.stream()
                .map(MemoryPair::getMemory)
                .collect(Collectors.toList());
    }

    public PredicateSensor sensingCondition(Predicate<BaseCore> predicate){
        this.predicate = predicate;
        return this;
    }

    public PredicateSensor affectsMemories(List<MemoryPair<?>> memories){
        this.memories = memories;
        return this;
    }

    @Override
    protected void sense(ServerWorld level, BaseCore entity) {
        if(predicate.test(entity)){

            this.memories.forEach((p)-> {
                p.testPassed(entity);
            });
        }else{
            this.memories.forEach((p)-> {
                p.testNotPassed(entity);
            });
        }
    }

    @Getter
    public static class MemoryPair<T>{
        MemoryModuleType<T> memory;
        Function<BaseCore, T> memoryValueSupplier;

        private MemoryPair(MemoryModuleType<T> memory, Function<BaseCore, T> memoryValueSupplier){
            this.memory = memory;
            this.memoryValueSupplier = memoryValueSupplier;
        }

        public static <T> MemoryPair<T> of(MemoryModuleType<T> memory, Function<BaseCore, T> memoryValueSupplier){
            return new MemoryPair<>(memory, memoryValueSupplier);
        }

        public void testPassed(BaseCore core){
            BrainUtils.setMemory(core.getBrain(), this.memory, memoryValueSupplier.apply(core));
        }

        public void testNotPassed(BaseCore entity) {
            BrainUtils.clearMemory(entity.getBrain(), this.memory);
        }
    }
}
