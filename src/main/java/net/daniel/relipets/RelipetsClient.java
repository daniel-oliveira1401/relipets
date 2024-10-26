package net.daniel.relipets;

import net.daniel.relipets.entity.cores.CyanCoreRenderer;
import net.daniel.relipets.registries.KeyBindingsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RelipetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RelipetsEntityRegistry.CYAN_CORE, CyanCoreRenderer::new);

        KeyBindingsRegistry.onInitialize();
    }
}
