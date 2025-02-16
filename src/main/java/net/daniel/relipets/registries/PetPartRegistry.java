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
        PART_VARIANTS.add(new PetPartRegistryEntry("arm_test"));

        //==== avian set (fighter mage) =======
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_basic_avian"
                , new ThrowEnemyUpAbility()
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_avian"
                , new CreateLockdownAreaAbility())
        );
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_avian"
                , new ThrowFireballAbility()
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_avian"
                , new HealSelfAbility()
        ));

        //====== quadruped set ('tanky' support) =======
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_basic_quadruped"
                , new SlowDownTargetAbility()
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_quadruped", new StrengthenSelfAbility()));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_quadruped", new CreateLockdownAreaAbility()));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_quadruped"));

        //======== bee set (mage based on effects) =========
        PART_VARIANTS.add(new PetPartRegistryEntry("leg_basic_bee", new SlowDownTargetAbility()));
        PART_VARIANTS.add(new PetPartRegistryEntry("torso_basic_bee", new StrengthenSelfAbility()));
        PART_VARIANTS.add(new PetPartRegistryEntry("head_basic_bee"));
        PART_VARIANTS.add(new PetPartRegistryEntry("tail_basic_bee", new PoisonTargetAbility()));
        PART_VARIANTS.add(new PetPartRegistryEntry("arm_basic_bee"
                , new SetEnemyOnFireAbility()
        ));
        PART_VARIANTS.add(new PetPartRegistryEntry("wing_basic_bee"
                , new ThrowEnemyUpAbility()
        ));
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
    }

}
