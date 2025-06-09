package net.daniel.relipets.registries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.daniel.relipets.entity.cores.abilities.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetPartRegistry {

    public static final List<PetPartRegistryEntry> PART_VARIANTS = new ArrayList<>();


    /*
    Pattern for abilities:



     */

    public static void onInitialize(){
        /*
        General rules for ability assignment:

            Torso -> Defensive ability
            Arm/Tail -> Offensive damaging ability
            Wing -> Crowd control
            Head -> utility
            Leg -> utility
         */
        //PART_VARIANTS.add(new PetPartRegistryEntry("arm_test"));

        //==== avian set (fighter mage) =======
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_basic_avian"
                , new ThrowEnemyUpAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_avian"
                , new HealOwnerAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_avian"
                , new ThrowFireballAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_avian"
                , new HealSelfAbility(), 1, 1.8f
        ));

        //====== quadruped set ('tanky' support) =======
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_basic_quadruped"
                , new SlowDownTargetAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_quadruped", new StrengthenSelfAbility(), 1, 1.8f));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_quadruped"
                , new HealOwnerAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_quadruped", 1.0f, 1.8f));

        //======== bee set (mage based on effects) =========
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_basic_bee", new SlowDownTargetAbility(), 1, 1.8f));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_bee", new StrengthenSelfAbility(), 1, 1.8f));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_bee", new HealOwnerAbility(), 1, 1.8f));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_bee", new PoisonTargetAbility(), 1, 1.8f));
        PART_VARIANTS.add(new PetPartRegistryEntry("arm_basic_bee"
                , new SetEnemyOnFireAbility(), 1, 1.8f
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_basic_bee"
                , new ThrowEnemyUpAbility(), 1, 1.8f
        ));

        //============= Standard Avian Set (yellow core) ================
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_standard_avian", new SlowDownTargetAbility(), 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_standard_avian", new StrengthenSelfAbility(), 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_standard_avian", new HealOwnerAbility(), 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_standard_avian", new PoisonTargetAbility(), 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_standard_avian"
                , new ThrowEnemyUpAbility(), 0.5f, 1.0f
        ));

        //============== Standard Dragon Set (yellow core)
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_standard_dragon", 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_standard_dragon", 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_standard_dragon", 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_standard_dragon", 0.5f, 1.0f));
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_standard_dragon", 0.5f, 1.0f));

    }

    public static Optional<PetPartRegistryEntry> getPartRegistryEntryByVariantId(String variantId){

        return PART_VARIANTS.stream().filter((p)-> p.name.equals(variantId)).findFirst();

    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class PetPartRegistryEntry{
        final private String name;

        @Nullable private CoreAbility ability;

        final float cyanCoreScale;
        final float yellowCoreScale;
    }

}

/*

Idea:

    Should abilities be tied to specific parts?
    Is it a good experience to be forced to have a given part in your pet if you want a given ability?

    Would it be better to have a ui where you can choose from a list of abilities which ones you want to have?
    Like an enchanting table, but instead of applying an enchantment to a part you would apply an ability to it.

    Or maybe, instead of applying the ability to the part, you could apply the ability to the core itself, and in
    order for the core to use it they would need to have a "part" in that slot which would "cast" it.

    The stats of the ability would then be the base stats of the core plus the stats of the part that casted the ability
    That way it would still matter which part you put because of their stats, but it wouldnt be a restriction. If you
    really want to use a given part, then you can. You just gotta check which stats it has and it is going to be
    beneficial to have it there.

    Also, each part of the core can have a set of abilities that the player can choose from.
    And also, some abilities would only be usable in cyan cores while others could be used only in the yellow core
    and at last some could be used on both.

    Yeah, i like that! Maybe i should also allow for the management of pet parts in there?


 */
