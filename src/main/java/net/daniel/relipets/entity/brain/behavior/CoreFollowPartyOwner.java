package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreFollowPartyOwner extends ExtendedBehaviour<BaseCore> {

    private int minDist = 5;
    private int maxDist = 30;

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT));
    }

    public CoreFollowPartyOwner(int minDist, int maxDist){
        this.minDist = minDist;
        this.maxDist = maxDist;
    }

    public CoreFollowPartyOwner(){

    }

    {
        this.startCondition = (entity)-> {
            PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);

            return entity.squaredDistanceTo(owner) > this.minDist * this.minDist && entity.squaredDistanceTo(owner) <= this.maxDist * this.maxDist;

        };

        this.runFor((e)-> 200);
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);
        Relipets.LOGGER.info("Starting FollowPartyOwner");
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        entity.getNavigation().startMovingTo(owner, 0.5);
    }

    @Override
    protected boolean shouldKeepRunning(BaseCore entity) {
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);


        return entity.squaredDistanceTo(owner) > this.minDist * this.minDist && entity.squaredDistanceTo(owner) <= this.maxDist * this.maxDist;
    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        Relipets.LOGGER.info("Stopping FollowPartyOwner");
        entity.getNavigation().stop();
    }

    @Override
    protected boolean shouldRun(ServerWorld level, BaseCore entity) {
        return super.shouldRun(level, entity);
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);
        if(!entity.getNavigation().isFollowingPath()){
            PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
            if(owner != null){
                entity.getNavigation().startMovingTo(owner, 0.5);
            }
        }
    }
}
