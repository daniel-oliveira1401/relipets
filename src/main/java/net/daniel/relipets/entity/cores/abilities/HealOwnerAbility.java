package net.daniel.relipets.entity.cores.abilities;

import net.daniel.relipets.entity.brain.memory.RelipetsMemoryTypes;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.entity.player.PlayerEntity;
import net.tslat.smartbrainlib.util.BrainUtils;

public class HealOwnerAbility implements CoreAbility {
    @Override
    public void start(CoreAbilityStats stats, BaseCore core) {
        PlayerEntity player = BrainUtils.getMemory(core.getBrain(), RelipetsMemoryTypes.PARTY_OWNER);

        if(player != null){
            player.setHealth(Math.min(player.getHealth() + 2, player.getMaxHealth()));
        }

    }
}
