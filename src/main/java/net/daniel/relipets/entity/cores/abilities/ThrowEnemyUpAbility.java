package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.brain.behavior.abilities.AbilityBehavior;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.util.math.Vec3d;
import net.tslat.smartbrainlib.util.BrainUtils;

public class ThrowEnemyUpAbility implements CoreAbility {

    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {

        LivingEntity target = BrainUtils.getMemory(core, MemoryModuleType.ATTACK_TARGET);

        if(target != null){
            Vec3d originalVelocity = target.getVelocity();
            target.setVelocity(
                    0,
                    originalVelocity.getY() + 1 * stats.getAbilityStrength(),
                    0
            );
        }
    }

}
