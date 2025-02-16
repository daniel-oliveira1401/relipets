package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.tslat.smartbrainlib.util.BrainUtils;

public class SlowDownTargetAbility implements CoreAbility {
    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        LivingEntity attackTarget = BrainUtils.getMemory(core.getBrain(), MemoryModuleType.ATTACK_TARGET);

        if(attackTarget != null){
            attackTarget.addStatusEffect(
                    new StatusEffectInstance(
                            StatusEffects.SLOWNESS,
                            Math.round(Utils.secondToTick(5) * stats.getAbilityDuration()),
                            Math.round(1 * stats.getAbilityStrength())));
        }

    }
}
