package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreFollowPartyOwner extends ExtendedBehaviour<BaseCore> {

    private final int tpDist;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.SHOULD_FOLLOW_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT)
        );
    }

    public CoreFollowPartyOwner(int tpDistance) {
        this.tpDist = tpDistance;
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);
        Relipets.LOGGER.info("Starting FollowPartyOwner");
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
        setWalkTarget(owner, entity);
    }


    @Override
    protected boolean shouldKeepRunning(BaseCore entity) {
        return Boolean.TRUE.equals(BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.SHOULD_FOLLOW_OWNER));
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        Relipets.LOGGER.info("Stopping FollowPartyOwner");
        entity.getNavigation().stop();
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        if (owner != null) {
            if (entity.squaredDistanceTo(owner) > this.tpDist * this.tpDist) {
                entity.teleport(owner.getX(), owner.getY(), owner.getZ());
                entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
                setWalkTarget(owner, entity);
                clearTarget(entity);
            } else if (!entity.getNavigation().isFollowingPath()) {
                entity.getNavigation().startMovingTo(owner, entity.getMovementSpeed());
                setWalkTarget(owner, entity);
            }
        }
    }

    private void setWalkTarget(PlayerEntity owner, BaseCore entity) {
        BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(owner, entity.getMovementSpeed(), 1));
    }

    private void clearTarget(BaseCore entity) {
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);
    }
}
