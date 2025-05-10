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
