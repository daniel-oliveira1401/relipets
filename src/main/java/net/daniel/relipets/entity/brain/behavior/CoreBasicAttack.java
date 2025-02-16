package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreBasicAttack extends ExtendedBehaviour<BaseCore> {
    int attackRange = 2;
    public static final List<Pair<MemoryModuleType<?>, MemoryModuleState>> MEMORIES = List.of(
            Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT),
            Pair.of(RelipetsMemoryTypes.WANTS_TO_USE_ABILITY, MemoryModuleState.VALUE_ABSENT)
    );

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return MEMORIES;
    }

    public CoreBasicAttack(int attackRange){
        this.attackRange = attackRange;
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);

        LivingEntity target = BrainUtils.getMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);

        if(target == null) return;

        if(entity.squaredDistanceTo(target) <= this.attackRange * this.attackRange || entity.getBoundingBox().intersects(target.getBoundingBox())){
            entity.getNavigation().stop();
            BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
            entity.getLookControl().lookAt(target);
            entity.performBasicAttack(target);
        }else{
            entity.getNavigation().startMovingTo(target, entity.getMovementSpeed());
            entity.getLookControl().lookAt(target);
            BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(target, entity.getMovementSpeed(), 1));
        }
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }
}
