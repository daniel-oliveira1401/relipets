package net.daniel.relipets.entity.brain.behavior.special;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.behavior.ContinuousTimedBehavior;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.brain.sensor.models.BehaviorDefinition;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreComeCloseToOwnerBehavior extends SpecialBehavior {

    int minDistance = 1;

    public CoreComeCloseToOwnerBehavior(){
        super(BaseCore.COME_CLOSE_TO_OWNER);
    }

    public CoreComeCloseToOwnerBehavior(int minDistance){
        super(BaseCore.COME_CLOSE_TO_OWNER);
        this.minDistance = minDistance;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER_NEARBY, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT)
                );
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);

        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        if(owner != null){
            entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
            BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(owner.getPos(), entity.getMovementSpeed(), 1));

        }
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);

        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        if(owner != null){
            if(entity.squaredDistanceTo(owner) > this.minDistance * this.minDistance){
                entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
                BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(owner.getPos(), entity.getMovementSpeed(), minDistance));
            }
        }

    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }
}
