package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.player.PlayerEntity;

public class CCAComponentsFactory implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, CardinalComponentsRegistry.PET_OWNER_KEY, f -> new PetOwnerComponent());
    }
}
