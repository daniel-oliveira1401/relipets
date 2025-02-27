package net.daniel.relipets.config;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "relipets-config", wrapperName = "RelipetsConfig")
public class RelipetsConfigModel {
    public String[] partModelVariants = {
            "arm_test",

            //==== avian set =======
            "wing_basic_avian",
            "head_basic_avian",
            "tail_basic_avian",
            "torso_basic_avian",

            //====== quadruped set =======
            "leg_basic_quadruped",
            "torso_basic_quadruped",
            "head_basic_quadruped",
            "tail_basic_quadruped",

            //======== bee set =========
            "leg_basic_bee",
            "torso_basic_bee",
            "head_basic_bee",
            "tail_basic_bee",
            "arm_basic_bee",
            "wing_basic_bee"

    }; //Each part model variant should be named as such: parttype_part_name
}
