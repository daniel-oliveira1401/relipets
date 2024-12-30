package net.daniel.relipets.entity.brain.sensor;

import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class FightWithOwnerSensor extends ExtendedSensor<BaseCore> {

    public static final List<MemoryModuleType<?>> MEMORIES = List.of(MemoryModuleType.ATTACK_TARGET);

    private int atkDist = 10;

    public FightWithOwnerSensor(){}

    public FightWithOwnerSensor atkDist(int atkDist){
        this.atkDist = atkDist;
        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return RelipetsSensorTypes.FIGHT_WITH_OWNER.get();
    }
    @Override
    protected void sense(ServerWorld level, BaseCore entity) {
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        LivingEntity attackTarget = BrainUtils.getMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);

        if(owner != null && attackTarget == null){
            //the one the owner is attacking
            LivingEntity attackingTarget = owner.getAttacking();

            //the one attacking the owner
            if(attackingTarget == null)
                attackingTarget = owner.getAttacker();

            if(attackingTarget != null && owner.squaredDistanceTo(attackingTarget) < atkDist * atkDist){
                //so that it doesnt attack itself
                if(attackingTarget != entity){
                    BrainUtils.setForgettableMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET, owner.getAttacking(), Utils.secondToTick(5));
                    System.out.println("Set core to attack owner's target");

                }
            }
//            else{
//                BrainUtils.clearMemory(entity.getBrain(), MemoryModuleType.ATTACK_TARGET);
//            }
        }

    }
}
