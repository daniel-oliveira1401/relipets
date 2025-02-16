package net.daniel.relipets.entity.brain.behavior.abilities;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

public abstract class AbilityBehavior extends ExtendedBehaviour<BaseCore> {

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT)
        );
    }

    @Override
    protected boolean shouldKeepRunning(BaseCore entity) {
        return true;
    }
}
