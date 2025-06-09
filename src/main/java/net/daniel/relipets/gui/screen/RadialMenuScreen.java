package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RadialMenuScreen extends BaseOwoScreen<FlowLayout> {

    FlowLayout rootComponent;

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    Identifier levelPointsScreen = new Identifier(Relipets.MOD_ID, "textures/gui/level_points_screen.png");
    Identifier petGroupsScreen = new Identifier(Relipets.MOD_ID, "textures/gui/pet_groups_screen.png");
    Identifier reorderPets = new Identifier(Relipets.MOD_ID, "textures/gui/reorder_pets_screen.png");
    Identifier recallAll = new Identifier(Relipets.MOD_ID, "textures/gui/recall_all.png");
    Identifier managementScreen = new Identifier(Relipets.MOD_ID, "textures/gui/management_screen.png");

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.flat(0x00000000));
        rootComponent.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.rootComponent = rootComponent;

        if (this.client == null || this.client.player == null) return;
        int size = 180;
        size = Math.min(this.height / 2 - 30, size);

        int menuOriginX = this.width / 2;
        int menuOriginY = this.height / 2;
        int totalCount = 6;
        rootComponent.child(new RadialMenuSectionWidget(
                menuOriginX, menuOriginY,
                size, 0, totalCount,
                reorderPets, 32, "Reorder Pets",
                this::goToReorderPetsScreen));
        rootComponent.child(new RadialMenuSectionWidget(menuOriginX, menuOriginY,
                size, 1, totalCount,
                petGroupsScreen, 32, "Pet Groups",
                this::goToGroupsScreen));
//        rootComponent.child(new RadialMenuSectionWidget(menuOriginX, menuOriginY,
//                size, 2, totalCount,
//                levelPointsScreen, 32, "Level Points",
//                this::goToLevelPointsScreen));

        rootComponent.child(new RadialMenuSectionWidget(menuOriginX, menuOriginY,
                size, 2, totalCount,
                recallAll, 32, "Recall All Pets",
                this::recallAllPets));

        rootComponent.child(new RadialMenuSectionWidget(menuOriginX, menuOriginY,
                size, 3, totalCount,
                managementScreen, 32, "Pet Management",
                this::goToPetRecoveryScreen));

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

    }

    private void goToPetRecoveryScreen(){
        if(this.client != null && this.client.player != null){
            this.client.setScreen(new PetManagementScreen(this));
        }
    }

    private void goToLevelPointsScreen(){

        if(this.client != null && this.client.player != null){
            //this.client.setScreen(new LevelPointsScreen(this));

        }
    }

    private void recallAllPets(){
        ClientPlayNetworking.send(C2SPacketHandlers.RECALL_ALL_PETS, PacketByteBufs.empty());
    }

    private void goToReorderPetsScreen(){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new ReorderPetsScreen(this));

        }
    }

    private void goToGroupsScreen(){

        if(this.client != null && this.client.player != null){
            this.client.setScreen(new PetGroupsScreen(this));

        }
    }
}

/*

Tessellator -> the guy that draws things on the screen

    - Tessellator.getInstance()

BufferBuilder -> this is where you compose whatever you want to draw. Lets say you want to draw a rectangle with
a border on it. You would first add the rectangle to the buffer builder, then add your borders. That would result
in a buffer containing a rectangle with a border. Thats why it is a builder. The tessellator is used to create a
buffer builder.

The buffer builder must be initialized using Tessellator.begin(). This method accepts a vertex format and a draw mode.
It then returns a buffer builder that is configured to use that vertex format and draw mode to draw things.


 */