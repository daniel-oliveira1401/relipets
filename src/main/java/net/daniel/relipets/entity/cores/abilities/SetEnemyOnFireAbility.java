package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.brain.behavior.abilities.AbilityBehavior;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.tslat.smartbrainlib.util.BrainUtils;

public class SetEnemyOnFireAbility implements CoreAbility {

    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        LivingEntity target = BrainUtils.getMemory(core, MemoryModuleType.ATTACK_TARGET);

        if(target != null){
            target.setOnFireFor(Math.round(4 * stats.getAbilityDuration()));
        }
    }

}
