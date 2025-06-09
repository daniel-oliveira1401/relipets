package net.daniel.relipets;

import net.daniel.relipets.entity.cores.client.CyanCoreProjectileRenderer;
import net.daniel.relipets.entity.cores.client.CyanCoreRenderer;
import net.daniel.relipets.entity.cores.client.YellowCoreRenderer;
import net.daniel.relipets.entity.util.PetCameraEntity;
import net.daniel.relipets.gui.hud.NewPetHud;
import net.daniel.relipets.gui.screen.PartManagementScreen;
import net.daniel.relipets.items.client.PetificatorProjectileRenderer;
import net.daniel.relipets.registries.GuiRelatedStuffRegistry;
import net.daniel.relipets.registries.KeyBindingsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.daniel.relipets.registries.S2CPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.util.math.Vec3d;

public class RelipetsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(RelipetsEntityRegistry.CYAN_CORE, CyanCoreRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.YELLOW_CORE, YellowCoreRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.PETIFICATOR_PROJECTILE, PetificatorProjectileRenderer::new);
        EntityRendererRegistry.register(RelipetsEntityRegistry.CYAN_CORE_PROJECTILE, CyanCoreProjectileRenderer::new);

        KeyBindingsRegistry.onInitialize();
        S2CPacketHandlers.onInitialize();
        //HudRenderCallback.EVENT.register(PetHud::renderCallback);
        HudRenderCallback.EVENT.register(NewPetHud::tick);
        HandledScreens.register(GuiRelatedStuffRegistry.PART_MANAGEMENT_SCREEN_HANDLER_TYPE, PartManagementScreen::new);
    }
}
