package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

public abstract class ContinuousTimedBehavior extends ExtendedBehaviour<BaseCore> {

    @Override
    protected boolean shouldKeepRunning(ServerWorld level, BaseCore entity, long gameTime) {
        return true;
    }
}
