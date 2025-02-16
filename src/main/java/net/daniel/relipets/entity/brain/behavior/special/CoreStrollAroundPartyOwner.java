package net.daniel.relipets.entity.brain.behavior.special;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.util.math.Vec3d;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreStrollAroundPartyOwner extends SpecialBehavior {


    public CoreStrollAroundPartyOwner(){
        super(BaseCore.STROLL_AROUND);
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER_NEARBY, MemoryModuleState.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT)
        );
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);
        int tries = 5;
        Vec3d targetPos = null;

        while(tries > 0){

            targetPos = NoPenaltyTargeting.find(entity, 5, 5);

            if(targetPos != null) break;

            tries--;
        }

        if(targetPos != null){
            entity.getNavigation().startMovingTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), entity.getMovementSpeed());
            BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(targetPos, entity.getMovementSpeed(), 1));
        }
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }
}
