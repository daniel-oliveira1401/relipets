package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class ReorderPetsScreen extends BaseOwoScreen<FlowLayout> {

    static final Identifier slotBg = new Identifier(Relipets.MOD_ID, "textures/gui/slot_bg.png");

    static int slotSize = 22;
    BaseOwoScreen<FlowLayout> parent;
    private FlowLayout rootComponent;
    private int rows;
    private GridLayout body;
    private PetPartyUpdateNotifier.Subscriber sub;
    private PetParty party;
    private boolean needsUpdate;

    public ReorderPetsScreen(BaseOwoScreen<FlowLayout> parent){
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

                EntityComponent<LivingEntity> component = Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
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

            slotContainer.children().forEach((e)-> e.remove());

            PetData petData = party.getSlotManager().getSlotAt(i).getContent();

            if(petData != null && petData.getPetEntityData().isValid()){
                Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

                EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

                EntityComponent<LivingEntity> component = Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
                        .scaleToFit(true).allowMouseRotation(true);

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
            this.needsUpdate = true;
//            this.rootComponent.queue(()-> {
//                this.client.execute(()-> updateSlots(p));
//            });
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
                Components.label(Text.of("Click on a slot to select it. Then click on the slot you wish to move the first slot to."))
                        .maxWidth(150).horizontalTextAlignment(HorizontalAlignment.CENTER).margins(Insets.bottom(20))
        );
        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();
        //6 slots per row

        this.rows = (int) Math.ceil((double) party.getSlotManager().getSlotCount() / columnCount);

        this.body = Containers.grid(Sizing.content(), Sizing.content(), rows, columnCount);
        body.padding(Insets.both(25, 25));
        body.surface(Surface.PANEL_INSET);
        body.id("grid");

        buildSlots();

        //container for the grid (for scrolling)
        bodyContainer.child(
                Containers.verticalScroll(Sizing.content(), Sizing.fixed(200), body)
        );

        rootComponent.child(bodyContainer);

        //build a grid containing all the pet slots
        // # # # # #
        // # # # # #

        //when you click a pet slot it becomes the selected slot

        //if you click the same slot again it will unselect the slot

        //if you click on another slot while you have a slot selected, the contents of
        //the selected slot will be transferred to the target slot
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if(needsUpdate){
            this.needsUpdate = false;
            this.updateSlots(this.party);
        }

        for(int i = 0; i < this.body.children().size(); i++){

            FlowLayout slotContainer = (FlowLayout) this.body.children().get(i);

            slotContainer.children().forEach((c)-> {
                if(c instanceof EntityComponent<?> entityComponent){
                    entityComponent.entity().tick();
                }

            });

        }

    }

    int selectedSlot = -1;

    public boolean onSlotClicked(int slotIndex){
        //check if there is a slot selected
        //if there isnt, then select one
        if(this.selectedSlot == slotIndex){
            this.selectedSlot = -1;
        }else if(this.selectedSlot == -1){
            this.selectedSlot = slotIndex;
        }else {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(this.selectedSlot); //origin
            buf.writeInt(slotIndex); //destination
            ClientPlayNetworking.send(C2SPacketHandlers.REORDER_PETS, buf);
            this.selectedSlot = -1;
        }

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
