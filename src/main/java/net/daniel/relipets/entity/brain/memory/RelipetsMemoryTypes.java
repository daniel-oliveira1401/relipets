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

    static {
        PARTY_OWNER = register("party_owner");
        PARTY_OWNER_NEARBY = register("party_owner_nearby");
    }


    private static <T> MemoryModuleType<T> register(String id) {
        return Registry.register(Registries.MEMORY_MODULE_TYPE, new Identifier(id), new MemoryModuleType<T>(Optional.empty()));
    }

    public static void init(){}

}
