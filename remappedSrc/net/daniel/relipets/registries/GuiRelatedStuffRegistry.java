package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.gui.screen.PartManagementScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class GuiRelatedStuffRegistry {

    public static final ScreenHandlerType<PartManagementScreenHandler> PART_MANAGEMENT_SCREEN_HANDLER_TYPE =
            new ScreenHandlerType<>(PartManagementScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void onInitialize(){
        Registry.register(Registries.SCREEN_HANDLER, new Identifier(Relipets.MOD_ID, "part_management"), PART_MANAGEMENT_SCREEN_HANDLER_TYPE);
    }
}
