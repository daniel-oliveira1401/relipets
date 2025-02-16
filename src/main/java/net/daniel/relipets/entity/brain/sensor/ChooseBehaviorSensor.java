package net.daniel.relipets.entity.brain.sensor;

import net.daniel.relipets.entity.brain.sensor.models.BehaviorDefinition;
import net.daniel.relipets.entity.brain.sensor.models.WeightedEntry;
import net.daniel.relipets.entity.brain.sensor.models.WeightedList;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.object.SBLShufflingList;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * This sensor takes a weighted list as input and will make a random weighted selection
 *
 */
public class ChooseBehaviorSensor extends ExtendedSensor<BaseCore> {
    private final MemoryModuleType<BehaviorDefinition> memory;
    private WeightedList<BehaviorDefinition> choices = new WeightedList<>();
    public int lastSelectedBehaviorDuration = 0;

    public ChooseBehaviorSensor(MemoryModuleType<BehaviorDefinition> memory){
        this.memory = memory;
    }

    public ChooseBehaviorSensor scanRateBetween(int max, int min){
        this.setScanRate((e) -> (int) (Math.random() * max + min) + this.lastSelectedBehaviorDuration);
        return this;
    }

    public ChooseBehaviorSensor withChoices(WeightedList<BehaviorDefinition> list){
        this.choices = list;

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
        if(!BrainUtils.hasMemory(entity.getBrain(), this.memory) &&
                !BrainUtils.hasMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET)){

            WeightedEntry<BehaviorDefinition> selected = this.choices.pickRandom();

            if(selected != null){
                BrainUtils.setMemory(entity.getBrain(), this.memory, selected.getValue());
                this.lastSelectedBehaviorDuration = selected.getValue().getDuration();
            }
        }
    }
}
