package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class MainPetificatorScreen extends BaseOwoScreen<FlowLayout> {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    //TODO: change layout to use huge panes instead of smol buttons
    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        LabelComponent label = Components.label(Text.literal("Choose what you want to configure"));
        label.margins(Insets.bottom(15));
        rootComponent.child(label);

        if(this.client == null || this.client.player == null) return;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetData selectedPet = petOwner.getPetParty().getSelectedPet();

        GridLayout buttonsDisplay = Containers.grid(Sizing.content(), Sizing.content(), 2, 2);
        rootComponent.child(buttonsDisplay);
        String levelPointsText = "Level Points";

        if(selectedPet != null){

            if(selectedPet.isSummonedNoEntityValidation()){
                levelPointsText += " (" + selectedPet.getPetInfo().getPetName() + ")";
            }else{
                levelPointsText += " (Requires pet summoned)";
            }

        }else{
            levelPointsText += " (Requires pet selected)";
        }

        FlowLayout levelPointsPane = createButtonPane(levelPointsText, ()-> this.goToLevelPointsScreen(null));
        buttonsDisplay.child(levelPointsPane, 0, 0);

        FlowLayout groupsBtn = createButtonPane("Pet Groups", ()-> this.goToGroupsScreen(null));
        buttonsDisplay.child(groupsBtn, 0, 1);

        FlowLayout reorderBtn = createButtonPane("Reorder Pets", ()-> this.goToReorderPetsScreen(null));
        buttonsDisplay.child(reorderBtn, 1, 0);
    }

    public FlowLayout createButtonPane(String text, Runnable btnCallback){
        FlowLayout paneContainer = Containers.verticalFlow(Sizing.fixed(120), Sizing.fixed(120));
        paneContainer.surface(Surface.PANEL);
        paneContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        paneContainer.margins(Insets.of(10));
        paneContainer.mouseDown().subscribe((a, b, c)-> {btnCallback.run(); return true;});
        paneContainer.child(
                Components.label(Text.of(text)).maxWidth(90).horizontalTextAlignment(HorizontalAlignment.CENTER).color(Color.BLACK)
        );
        return paneContainer;
    }

    private void goToLevelPointsScreen(ButtonComponent buttonComponent){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new LevelPointsScreen(this));

        }
    }

    private void goToReorderPetsScreen(ButtonComponent buttonComponent){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new ReorderPetsScreen(this));

        }
    }

    private void goToGroupsScreen(ButtonComponent buttonComponent){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new PetGroupsScreen(this));

        }
    }
}