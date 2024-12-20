package net.daniel.relipets.entity.brain.memory;

import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class RelipetsMemoryTypes {

    public static final MemoryModuleType<PlayerEntity> PARTY_OWNER;
    public static final MemoryModuleType<Boolean> PARTY_OWNER_NEARBY;
    public static final MemoryModuleType<Boolean> SHOULD_TEST;
    public static final MemoryModuleType<Boolean> SHOULD_BEHAVE;
    public static final MemoryModuleType<Boolean> SHOULD_USE_ABILITY;


    static {
        PARTY_OWNER = register("party_owner");
        PARTY_OWNER_NEARBY = register("party_owner_nearby");
        SHOULD_TEST = register("should_test");
        SHOULD_BEHAVE = register("should_behave");
        SHOULD_USE_ABILITY = register("should_use_ability");
    }


    private static <T> MemoryModuleType<T> register(String id) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, new Identifier(id), new MemoryModuleType<T>(Optional.empty()));
    }

    public static void init(){}

}
