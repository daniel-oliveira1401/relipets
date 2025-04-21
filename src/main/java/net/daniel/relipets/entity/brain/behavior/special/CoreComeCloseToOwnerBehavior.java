package net.daniel.relipets.entity.brain.behavior.special;

import com.mojang.datafixers.util.Pair;
import jdk.jshell.execution.Util;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.entity.brain.behavior.ContinuousTimedBehavior;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.brain.sensor.models.BehaviorDefinition;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreComeCloseToOwnerBehavior extends SpecialBehavior {

    int minDistance = 1;
    int tpDistance = 20;

    public CoreComeCloseToOwnerBehavior(){
        super(BaseCore.COME_CLOSE_TO_OWNER);
    }

    public CoreComeCloseToOwnerBehavior(int minDistance, int tpDistance){
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
            double distance = entity.squaredDistanceTo(owner);

            //entity is too far away. Teleport it close to the owner
            if(distance > this.tpDistance * this.tpDistance){
                BlockPos safePosToTp = Utils.findRandomSafePositionAroundPlayer((ServerWorld) entity.getWorld(), owner.getBlockPos(), 5, entity.getWorld().getRandom());

                if(safePosToTp == null){
                    safePosToTp = owner.getBlockPos();

                }

                entity.teleport(
                        safePosToTp.getX(),
                        safePosToTp.getY(),
                        safePosToTp.getZ()
                );

            }else if(distance > this.minDistance * this.minDistance){
                //entity is far but not too far away
                BlockPos positionNearPlayer = Utils.findRandomSafePositionAroundPlayer((ServerWorld) entity.getWorld(), owner.getBlockPos(), 5, entity.getWorld().getRandom());

                if(positionNearPlayer != null){
                    entity.getNavigation().startMovingTo(positionNearPlayer.getX(), positionNearPlayer.getY(),positionNearPlayer.getZ(), entity.getMovementSpeed());

                    BrainUtils.setMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET, new WalkTarget(positionNearPlayer, entity.getMovementSpeed(), minDistance));
                }
            }else{
                entity.getNavigation().stop();
            }
        }

    }

    @Override
    protected void stop(BaseCore entity) {
        super.stop(entity);
        BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.WALK_TARGET);
    }
}
