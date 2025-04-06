package net.daniel.relipets;

import net.daniel.relipets.entity.cores.client.CyanCoreRenderer;
import net.daniel.relipets.entity.cores.client.YellowCoreRenderer;
import net.daniel.relipets.gui.hud.NewPetHud;
import net.daniel.relipets.items.client.PetificatorProjectileRenderer;
import net.daniel.relipets.registries.KeyBindingsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.daniel.relipets.registries.S2CPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class RelipetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RelipetsEntityRegistry.CYAN_CORE, CyanCoreRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.YELLOW_CORE, YellowCoreRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.PETIFICATOR_PROJECTILE, PetificatorProjectileRenderer::new);

        KeyBindingsRegistry.onInitialize();
        S2CPacketHandlers.onInitialize();
        //HudRenderCallback.EVENT.register(PetHud::renderCallback);
        HudRenderCallback.EVENT.register(NewPetHud::tick);
    }
}
