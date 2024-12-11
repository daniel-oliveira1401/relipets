package net.daniel.relipets.entity.brain.activity;

import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class CoreCustomActivities {

    public static final Activity TEST = register("test");

    private static Activity register(String id) {
        return Registry.register(Registries.ACTIVITY, id, new Activity(id));
    }

    public static void init(){}

}
