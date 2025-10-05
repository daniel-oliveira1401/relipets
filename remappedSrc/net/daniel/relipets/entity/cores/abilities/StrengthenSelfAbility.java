package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class StrengthenSelfAbility implements CoreAbility {
    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {

        core.addStatusEffect(new StatusEffectInstance(
                StatusEffects.STRENGTH,
                Math.round(Utils.secondToTick(10) * stats.getAbilityDuration()),
                Math.round(2 * stats.getAbilityStrength())));

    }
}
