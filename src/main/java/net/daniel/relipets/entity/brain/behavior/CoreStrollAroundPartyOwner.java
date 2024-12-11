package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class CoreStrollAroundPartyOwner extends ExtendedBehaviour<BaseCore> {


    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER_NEARBY, MemoryModuleState.VALUE_PRESENT));
    }

    {
        this.startCondition = (baseCoreEntity) -> {
            //it should be near the owner
            if(Boolean.FALSE.equals(BrainUtils.getMemory(baseCoreEntity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER_NEARBY))) return false;

            //it should have a random chance of happening
            return false;
        };
    }

}
