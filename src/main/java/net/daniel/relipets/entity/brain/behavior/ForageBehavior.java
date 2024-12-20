package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

public class ForageBehavior extends ExtendedBehaviour<BaseCore> {
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);
        System.out.println("[BEHAVIOR] Foraging...");
    }
}
