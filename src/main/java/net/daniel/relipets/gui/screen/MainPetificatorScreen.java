package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MainPetificatorScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        LabelComponent label = Components.label(Text.literal("Choose what you want to configure"));
        label.margins(Insets.bottom(15));
        rootComponent.child(label);

        ButtonComponent levelPointsBtn = Components.button(Text.literal("Level Points"), this::goToLevelPointsScreen);
        rootComponent.child(levelPointsBtn);
    }

    private void goToLevelPointsScreen(ButtonComponent buttonComponent){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new LevelPointsScreen(this));

        }
    }
}