package net.daniel.relipets.entity.brain.behavior.special;

import net.daniel.relipets.entity.brain.behavior.ContinuousTimedBehavior;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.brain.sensor.models.BehaviorDefinition;
import net.daniel.relipets.entity.cores.BaseCore;
import net.tslat.smartbrainlib.util.BrainUtils;

/**
 * A SpecialBehavior is a BaseCore specific behavior that will be performed once the BaseCore
 * feels like doing so. It is triggered by the ChooseBehaviorSensor.
 *
 * SpecialBehaviors should be things like strolling around, greeting nearby mobs, looking at a
 * flower and things like that.
 * Special behaviors will have a weight associated to them using a BehaviorDefinition and they will
 * be chosen taking that into account.
 *
 * The activation condition for a Special behavior is that the BEHAVIOR_TO_PERFORM memory has their
 * behavior definition. Once started, the behavior will run for the amount of time defined by their
 * definition. No other special behavior will be started until the current one is over
 */
public abstract class SpecialBehavior extends ContinuousTimedBehavior {

    public SpecialBehavior(String behaviorName){
        this.startCondition((e)-> {
            BehaviorDefinition definition = BrainUtils.getMemory(e, RelipetsMemoryTypes.BEHAVIOR_TO_PERFORM);
            if(definition != null){
                return behaviorName.equals(definition.getName());
            }
            return false;
        });
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);

        BehaviorDefinition def = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.BEHAVIOR_TO_PERFORM);

        if(def != null){
            this.runFor((e)-> def.getDuration());
        }
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), RelipetsMemoryTypes.BEHAVIOR_TO_PERFORM);
    }
}
