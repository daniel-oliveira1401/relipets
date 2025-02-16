package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.tslat.smartbrainlib.util.BrainUtils;

public class HealSelfAbility implements CoreAbility {
    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        core.heal(8);

    }
}
