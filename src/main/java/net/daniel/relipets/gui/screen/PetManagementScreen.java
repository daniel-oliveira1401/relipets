package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetMoveMode;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PetManagementScreen extends BaseOwoScreen<FlowLayout> {

    static final Identifier slotBg = new Identifier(Relipets.MOD_ID, "textures/gui/slot_bg.png");
    static int slotSize = 22;
    BaseOwoScreen<FlowLayout> parent;
    private FlowLayout rootComponent;
    private int rows;
    private GridLayout body;
    private PetPartyUpdateNotifier.Subscriber sub;
    private PetParty party;
    private @Nullable PetData selectedPetData;
    private ButtonComponent movementModeButton;
    private TextBoxComponent petName;

    public PetManagementScreen(BaseOwoScreen<FlowLayout> parent){
        this.parent = parent;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    public void buildSlots(){
        if(this.client == null || this.client.player == null || this.body == null) return;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();

        for(int i = 0; i <party.getSlotManager().getSlotCount(); i++){

            int currentRow = (int) Math.floor((double)i / columnCount);

            int currentColumn = i - currentRow * columnCount;

            Surface slotSurface = Surface.tiled(slotBg, slotSize, slotSize);

            FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize));
            slotContainer.margins(Insets.both(5, 5));
            slotContainer.surface(slotSurface);
            int finalI = i;
            slotContainer.mouseDown().subscribe((a, b, c)-> this.onSlotClicked(finalI));
            //.margins(Insets.right(slotSpacing));

            slotContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            PetData petData = petOwner.getPetParty().getSlotManager().getSlotAt(i).getContent();

            if(petData != null && petData.getPetEntityData().isValid()){
                Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

                EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

                EntityComponent component = Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
                        .scaleToFit(true);

                slotContainer.child(
                        component
                );
            }

            this.body.child(slotContainer, currentRow, currentColumn);
        }

    }

    public void updateSlots(PetParty party){
        if(this.client == null || this.client.player == null || this.body == null) return;

        for(int i = 0; i < this.body.children().size(); i++){

            FlowLayout slotContainer = (FlowLayout) this.body.children().get(i);

            slotContainer.clearChildren();

            PetData petData = party.getSlotManager().getSlotAt(i).getContent();

            if(petData != null && petData.getPetEntityData().isValid()){
                Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

                EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

                EntityComponent component = Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
                        .scaleToFit(true);

                slotContainer.child(
                        component
                );
            }
        }

    }

    int columnCount = 6;

    @Override
    public void removed() {
        super.removed();
        PetPartyUpdateNotifier.getInstance().unsubscribe(this.sub);
    }



    @Override
    protected void build(FlowLayout rootComponent) {
        if(this.client == null || this.client.player == null) return;

        this.sub = (p)-> {
            this.party = p;
            this.rootComponent.queue(()-> {
                updateSelectedPetBasedOnSlot();
                updateSlots(p);
                updateActionPanel();
            });
        };

        PetPartyUpdateNotifier.getInstance().subscribe(sub);

        this.rootComponent = rootComponent;

        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.padding(Insets.both(15, 15));

        ButtonComponent backBtn = Components.button(Text.of("< Back"), this::backToMainScreen);
        backBtn.margins(Insets.bottom(10));
        rootComponent.child(backBtn);

        var bodyContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
        bodyContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        bodyContainer.child(
                Components.label(Text.of("Pet Management"))
                        .maxWidth(150).horizontalTextAlignment(HorizontalAlignment.CENTER).margins(Insets.bottom(20))
        );
        bodyContainer.child(
                Components.label(Text.of("Select a pet from the grid below and use the options on the right ->"))
                        .maxWidth(150).horizontalTextAlignment(HorizontalAlignment.CENTER).margins(Insets.bottom(20))
        );
        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();
        this.party = party;
        //6 slots per row

        this.rows = (int) Math.ceil((double) party.getSlotManager().getSlotCount() / columnCount);

        this.body = Containers.grid(Sizing.content(), Sizing.content(), rows, columnCount);
        body.padding(Insets.both(25, 25));
        body.surface(Surface.PANEL_INSET);
        body.id("grid");

        buildSlots();

        this.movementModeButton = Components.button(Text.of("Movement: "), (b)-> this.cycleMovementMode());
        this.movementModeButton.sizing(Sizing.fill(100), Sizing.content());
        this.petName = Components.textBox(Sizing.fill(69));
        //container for the grid (for scrolling)
        bodyContainer.child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                    Containers.verticalScroll(Sizing.content(), Sizing.fixed(200), body)
                ).child(
                        Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.label(Text.of("Rename Pet")).margins(Insets.bottom(4)).sizing(Sizing.fill(100), Sizing.content())
                                ).child(
                                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                                                petName
                                        ).child(
                                                Components.button(Text.of("Rename"), (b)-> renamePet()).sizing(Sizing.fill(30), Sizing.content())
                                        ).sizing(Sizing.fill(100), Sizing.content())
                                ).margins(Insets.bottom(4)).sizing(Sizing.fill(100), Sizing.content())
                        ).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.button(Text.of("Locate Pet"), (b)-> locateSelectedPet()).sizing(Sizing.fill(100), Sizing.content())
                                ).margins(Insets.bottom(4)).sizing(Sizing.fill(100), Sizing.content())
                        ).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.button(Text.of("Release Pet"), (b)-> openPetReleaseModal()).sizing(Sizing.fill(100), Sizing.content())
                                ).margins(Insets.bottom(4)).sizing(Sizing.fill(100), Sizing.content())
                        ).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        movementModeButton
                                ).margins(Insets.bottom(15))
                        ).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.button(Text.literal("Recover Pet [!]").setStyle(
                                                Style.EMPTY.withColor(0xFF5E60)
                                        ), (b)-> openRecoverPetDialog()).sizing(Sizing.fill(100), Sizing.content())
                                ).margins(Insets.bottom(5)).sizing(Sizing.fill(100), Sizing.content())
                        ).margins(Insets.left(15)).sizing(Sizing.fixed(150), Sizing.content())
                )
        );

        rootComponent.child(bodyContainer);

        onSlotClicked(this.party.getSelectedPetIndex());

    }

    private void cycleMovementMode() {
        if(this.selectedPetData != null){

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.selectedSlot);

            ClientPlayNetworking.send(C2SPacketHandlers.CHANGE_MOVE_MODE, buf);
        }
    }

    private void locateSelectedPet() {
        if(this.client != null && this.client.player != null && this.client.world != null && this.selectedPetData != null){

            Entity entity = this.client.world.getEntityById(this.selectedPetData.getPetEntityData().getEntityId());
            if(entity != null){
                this.client.setCameraEntity(entity);
                this.client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                this.client.setScreen(null);
                Utils.setTimeout(()-> {
                    this.client.setCameraEntity(this.client.player);
                    this.client.options.setPerspective(Perspective.FIRST_PERSON);
                }, Utils.secondToTick(5));
            }
            Utils.message(this.selectedPetData.getPetInfo().getPetName() + " was last seen at: " + this.selectedPetData.getPetEntityData().getTracker().toString(), this.client.player);
        }
    }

    private void releasePet() {
        closeModal();
        PacketByteBuf buf = PacketByteBufs.create();

        if(this.selectedPetData != null){

            buf.writeInt(this.selectedSlot);

            ClientPlayNetworking.send(C2SPacketHandlers.RELEASE_PET, buf);

        }
    }

    private void openRecoverPetDialog() {
        if(this.selectedPetData == null) return;

        rootComponent.child(
                Containers.overlay(
                        Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                Components.label(Text.of("Pet Recovery")).color(Color.BLACK).margins(Insets.bottom(14))
                        ).child(
                                Components.label(Text.of("Pet Recovery is the process of forcibly spawning a copy of your pet by using the data that is stored. " +
                                        "THIS SHOULD ONLY BE USED IF YOU TRULY LOST YOUR PET AND CAN'T SUMMON IT THROUGH NORMAL MEANS."))
                                        .maxWidth(200).color(Color.BLACK).margins(Insets.bottom(5))
                        ).child(
                                Components.label(Text.of("Please make sure you tried summoning/recalling the pet you are trying to recover before using this tool. " +
                                        "This should only be a last measure and " +
                                        "using it in a scenario other than that is CHEATING.")).maxWidth(200).color(Color.BLACK).margins(Insets.bottom(5))
                        ).child(
                                        Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                            Components.label(Text.of("After you click the button, the recovered pet will spawn at the player location."))
                                                    .maxWidth(200).color(Color.BLACK)

                                        ).surface(Surface.PANEL).padding(Insets.of(10)).margins(Insets.bottom(14))
                        ).child(
                                Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.button(Text.of("Recover Pet"), (b)-> recoverPet()).margins(Insets.right(5))
                                ).child(
                                        Components.button(Text.of("Cancel"), (b)-> closeModal())
                                )
                        ).surface(Surface.PANEL).padding(Insets.of(10))
                ).id("overlay").zIndex(999)
        );
    }

    private void openPetReleaseModal(){

        if(this.selectedPetData == null) return;

        rootComponent.child(
                Containers.overlay(
                        Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                Components.label(Text.of("Release Pet")).color(Color.BLACK).margins(Insets.bottom(14))
                        ).child(
                                Components.label(Text.of("Are you sure you want to release [" + this.selectedPetData.getPetInfo().getPetName() + "]?"))
                                        .maxWidth(200).color(Color.BLACK).margins(Insets.bottom(5))
                        ).child(
                                Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                                        Components.button(Text.of("Release Pet"), (b)-> releasePet()).margins(Insets.right(5))
                                ).child(
                                        Components.button(Text.of("Cancel"), (b)-> closeModal())
                                )
                        ).surface(Surface.PANEL).padding(Insets.of(10))
                ).id("overlay").zIndex(999)
        );
    }

    public void closeModal(){
        @Nullable OverlayContainer<FlowLayout> overlay = rootComponent.childById(OverlayContainer.class, "overlay");
        if(overlay != null){
            overlay.remove();
        }
    }

    public void recoverPet(){
        closeModal();

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(this.selectedSlot);

        ClientPlayNetworking.send(C2SPacketHandlers.RECOVER_PET, buf);

    }

    private void renamePet() {
        PacketByteBuf buf = PacketByteBufs.create();
        TextBoxComponent textBox = rootComponent.childById(TextBoxComponent.class, "petName");
        if(textBox != null && this.selectedPetData != null){

            buf.writeInt(this.selectedSlot);
            buf.writeString(textBox.getText());

            ClientPlayNetworking.send(C2SPacketHandlers.RENAME_PET, buf);

        }

    }

    private void updateActionPanel() {

            if(this.selectedPetData != null){
                this.petName.text(this.selectedPetData.getPetInfo().getPetName());
                this.movementModeButton.setMessage(Text.of("Movement: "+ this.selectedPetData.getMoveMode().name()));
            }else{
                this.petName.text("");
                this.movementModeButton.setMessage(Text.of("Movement:"));
            }


    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        //iterate over the slots
        //  if the slot index matches the selected slot, then paint it with another color

    }

    int selectedSlot = -1;

    public void updateSelectedPetBasedOnSlot(){
        if(this.selectedSlot == -1){
            this.selectedPetData = null;
        }else{
            this.selectedPetData = this.party.getSlotManager().getSlotAt(this.selectedSlot).getContent();
        }


    }

    public boolean onSlotClicked(int slotIndex){

        this.selectedSlot = slotIndex;

        updateSelectedPetBasedOnSlot();
        updateActionPanel();

        if(this.rootComponent != null){
            if(body != null){

                for(int i = 0; i < body.children().size(); i++){

                    var slot = body.children().get(i);
                    if(slot instanceof FlowLayout flowSlot){

                        flowSlot.surface(Surface.tiled(slotBg, slotSize, slotSize));
                        if(i == this.selectedSlot){
                            flowSlot.surface(Surface.PANEL);
                        }
                    }

                }


            }
        }

        return true;
    }

    private void backToMainScreen(ButtonComponent btn){
        if(this.client != null){
            this.client.setScreen(this.parent);
        }
    }

}
