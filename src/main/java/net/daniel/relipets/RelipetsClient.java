package net.daniel.relipets;

import net.daniel.relipets.entity.cores.CyanCoreRenderer;
import net.daniel.relipets.gui.hud.NewPetHud;
import net.daniel.relipets.items.client.PetificatorProjectileRenderer;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.daniel.relipets.registries.KeyBindingsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class RelipetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RelipetsEntityRegistry.CYAN_CORE, CyanCoreRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.PETIFICATOR_PROJECTILE, PetificatorProjectileRenderer::new);

        KeyBindingsRegistry.onInitialize();
        //HudRenderCallback.EVENT.register(PetHud::renderCallback);
        HudRenderCallback.EVENT.register(NewPetHud::tick);
    }
}
