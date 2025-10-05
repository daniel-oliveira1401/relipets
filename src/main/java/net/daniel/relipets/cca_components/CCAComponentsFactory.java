package net.daniel.relipets.cca_components;

import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class CCAComponentsFactory implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        //registry.registerFor(PlayerEntity.class, CardinalComponentsRegistry.PET_OWNER_KEY, PetOwnerComponent::new);

        registry.beginRegistration(PlayerEntity.class, CardinalComponentsRegistry.PET_OWNER_KEY)
                .impl(PetOwnerComponent.class)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .end(PetOwnerComponent::new);

        registry.registerFor(LivingEntity.class, CardinalComponentsRegistry.PET_METADATA_KEY, PetMetadataComponent::new);
    }
}
