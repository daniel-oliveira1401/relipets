package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreComeCloseToOwnerBehavior extends ExtendedBehaviour<BaseCore> {
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER_NEARBY, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT));
    }

    @Override
    protected void start(BaseCore entity) {
        System.out.println("[BEHAVIOR] Coming close to owner...");
        PlayerEntity owner = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        entity.getNavigation().startMovingTo(owner, 0.5f);
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);
    }
}
