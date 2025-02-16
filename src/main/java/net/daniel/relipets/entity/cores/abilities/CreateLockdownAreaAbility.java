package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.tslat.smartbrainlib.util.BrainUtils;

public class CreateLockdownAreaAbility implements CoreAbility {
    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        LivingEntity attackTarget = BrainUtils.getMemory(core.getBrain(), MemoryModuleType.ATTACK_TARGET);
        if (attackTarget != null) {
            AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(core.getWorld(),
                    attackTarget.getX(),
                    attackTarget.getY(),
                    attackTarget.getZ());

            cloud.setRadius(3.0F * stats.getAbilityRange());

            cloud.setDuration(Math.round(200 * stats.getAbilityDuration())); // 10 seconds

            cloud.setWaitTime(0);

            cloud.setColor(0xcccccc);

            cloud.addEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Math.round(Utils.secondToTick(5) * stats.getAbilityDuration()), 4));
            cloud.addEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, Math.round(Utils.secondToTick(5) * stats.getAbilityDuration()), 2));

            core.getWorld().spawnEntity(cloud);

            System.out.println("used lockdown ability");

        }

    }

    @Override
    public int getCooldown(BaseCore core) {
        return 20;
    }
}
