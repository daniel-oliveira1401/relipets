package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreComeCloseToOwnerBehavior extends ContinuousTimedBehavior {
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
        System.out.println("[BEHAVIOR] Coming close to owner...");
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        if(owner != null){
            entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
            BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(owner.getPos(), entity.getMovementSpeed(), 1));
        }
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }
}
