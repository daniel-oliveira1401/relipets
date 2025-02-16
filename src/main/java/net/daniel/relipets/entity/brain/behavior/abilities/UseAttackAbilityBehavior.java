package net.daniel.relipets.entity.brain.behavior.abilities;

import com.mojang.datafixers.util.Pair;
import net.daniel.relipets.cca_components.PartSystemComponent;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.abilities.CoreAbility;
import net.daniel.relipets.entity.cores.abilities.CoreAbilityStats;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

/***
 * Behavior used for triggering attack abilities
 */
public class UseAttackAbilityBehavior extends ExtendedBehaviour<BaseCore> {
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



    @Override
    protected void start(BaseCore entity) {
        super.start(entity);

        //get abilities
        List<CoreAbility> abilities = entity.getPartSystem().getAllAbilities();

        //TODO: add a better way to choose which ability to run
        CoreAbility abilityToRun = abilities.get((int) Math.floor(Math.random() * abilities.size()));

        entity.runAbility(abilityToRun);

        PetMetadataComponent petMetadataComponent = CardinalComponentsRegistry.PET_METADATA_KEY.get(entity);

        petMetadataComponent.getLevelProgression().receiveXp(10);

        this.cooldownFor(abilityToRun::getCooldown);

    }
}
