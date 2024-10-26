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
            "torso_basic_avian"
    }; //Each part model variant should be named as such: parttype_part_name
}
