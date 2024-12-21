package net.daniel.relipets.entity.brain.behavior;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class LookAtPartyOwner extends ContinuousTimedBehavior {

    float chance = 0.5f;
    public LookAtPartyOwner(float chance){
        this.chance = chance;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryModuleState>> getMemoryRequirements() {
        return List.of(
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER, MemoryModuleState.VALUE_PRESENT),
                Pair.of(RelipetsMemoryTypes.PARTY_OWNER_NEARBY, MemoryModuleState.VALUE_PRESENT));
    }

    {
        this.startCondition = (e)-> Math.random() < this.chance;
    }

    @Override
    protected void start(BaseCore entity) {
        super.start(entity);
        System.out.println("Started look at party owner");
    }

    @Override
    protected void tick(BaseCore entity) {
        super.tick(entity);

        PlayerEntity player = BrainUtils.getMemory(entity.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);
        if(player != null){
            entity.getLookControl().lookAt(player);
        }
    }
}
