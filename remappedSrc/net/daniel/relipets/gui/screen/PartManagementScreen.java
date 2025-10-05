package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetInventoryManager;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PartManagementScreen extends BaseOwoHandledScreen<FlowLayout, PartManagementScreenHandler> {

    FlowLayout rootComponent;
    private PetData petData;
    private PetPartyUpdateNotifier.Subscriber sub;
    private int slot = -1;
    private boolean invalidSlot = true;
    private BaseCore entity;

    public PartManagementScreen(PartManagementScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, Text.empty());
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        //to remove the inventory titles
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.sub = (p)-> {
            if(this.invalidSlot){
                this.slot = p.getSelectedPetIndex();
                this.buildForReal();
                invalidSlot = false;
            }

            this.petData = p.getSlotManager().getSlotAt(this.slot).getContent();

            this.rootComponent.queue(()-> {
                //run update code here
                FlowLayout entityContainer = this.rootComponent.childById(FlowLayout.class, "entityContainer");
                if(entityContainer != null){

                    entityContainer.clearChildren();

                    entityContainer.child(
                            buildEntityComponent(petData)
                    );
                }
            });
        };
        PetPartyUpdateNotifier.getInstance().subscribe(sub);
        this.rootComponent = rootComponent;
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.padding(Insets.both(15, 15));


        if(this.client == null || this.client.player == null) return;

        ClientPlayNetworking.send(C2SPacketHandlers.GET_PARTY, PacketByteBufs.empty());

    }

    public void buildForReal(){
        if(this.client == null || this.client.player == null) return;

        PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        this.petData = petOwnerComponent.getPetParty().getSlotManager().getSlotAt(this.slot).getContent();



        //top row (core display and abilities)
        rootComponent.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(50)).child(
                        //Pet Entity display
                        Containers.verticalFlow(Sizing.fill(40), Sizing.fill(100)).child(
                                Components.label(Text.of(this.petData.getPetInfo().getPetName()))
                        ).child(
                                Containers.verticalFlow(Sizing.content(), Sizing.content()).child(
                                        buildEntityComponent(petData)
                                ).id("entityContainer")

                        ).surface(Surface.outline(0xffcccccc)).padding(Insets.both(5, 5)).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                ).child(
                        //Part Slots
                        buildPartSlots()
                ).child(
                        buildAbilityColumn()
                )

        );



        //bottom row (player inventory)
        rootComponent.child(
                Containers.verticalFlow(Sizing.fill(100), Sizing.fill(50)).child(
                        Components.label(Text.of("Player Inventory")).margins(Insets.bottom(10))
                ).child(
                        buildPlayerInventory()
                ).alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
        );
    }

    private ScrollContainer<FlowLayout> buildAbilityColumn() {

        FlowLayout container  = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        container.padding(Insets.of(5));
        for(int i = 0; i < 10; i++){
            int finalI = i;
            container.child(
                    Containers.verticalFlow(Sizing.fill(100), Sizing.content()).child(
                            Components.label(Text.of("Coming Soon...")).color(Color.BLACK).margins(Insets.bottom(5))
                    ).child(
                            Components.label(Text.of("In a near future, you will be able to infuse abilities into your part slots...")).color(Color.BLACK).sizing(Sizing.fill(100), Sizing.content())
                    ).child(
                            Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).child(
                                    Components.button(Text.of("Cool!!"), (b)-> System.out.println("Selected ability " + finalI))
                            ).horizontalAlignment(HorizontalAlignment.RIGHT)
                    ).padding(Insets.of(5)).surface(Surface.PANEL).margins(Insets.bottom(5))
            );

        }

        return Containers.verticalScroll(Sizing.fill(40), Sizing.fill(100), container);
    }

    public EntityComponent<BaseCore> buildEntityComponent(PetData petData){
        Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

        EntityType<BaseCore> entityType = (EntityType<BaseCore>) Registries.ENTITY_TYPE.get(entityTypeId);

        EntityComponent<BaseCore> entityComponent = Components.entity(Sizing.fixed(height/2 - (9 + 30)), entityType, petData.getPetEntityData().getEntityNbt())
                .scale(0.1f).allowMouseRotation(true);

        this.entity = entityComponent.entity();

        return entityComponent;
    }

    @Override
    public void render(DrawContext vanillaContext, int mouseX, int mouseY, float delta) {
        try{
            super.render(vanillaContext, mouseX, mouseY, delta);
        }catch (Exception e){
            Utils.log("Owo exploded...");
        }
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
    }

    //TODO: update slots when server pushes changes to client
    private FlowLayout buildPartSlots(){
        FlowLayout slotColumnContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        slotColumnContainer.verticalAlignment(VerticalAlignment.CENTER);
        //pet slots

        for(int i = 0; i < 6; i++){
            int finalI = i;
            PetInventoryManager.PetInventorySlot slot = this.entity.getPartSystem().getPetInventoryManager().getInventorySlotByIndex(finalI);
            slotColumnContainer.child(
                Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                    buildSlot(i)
                ).child(
                        Components.button(Text.of( slot.getData().getCompatiblePart() + " >"), (b)-> {

                            System.out.println("Selected slot "+ finalI + "with name: " + slot.getData().getName());
                        })
                ).verticalAlignment(VerticalAlignment.CENTER).allowOverflow(true)
            );
        }

        slotColumnContainer.margins(Insets.horizontal(10));
        return slotColumnContainer;
    }

    public FlowLayout buildSlot(int slotIndex){
        FlowLayout slotComponentContainer = Containers.verticalFlow(Sizing.content(), Sizing.content());
        slotComponentContainer.allowOverflow(true);
        slotComponentContainer.surface(Surface.DARK_PANEL);
        slotComponentContainer.padding(Insets.both(5, 5));
        SlotComponent slotComponent = this.slotAsComponent(slotIndex);
        slotComponentContainer.child(slotComponent);

        return slotComponentContainer;
    }

    private GridLayout buildPlayerInventory(){
        GridLayout inventoryGrid = Containers.grid(Sizing.content(), Sizing.content(), 4, 9);
        inventoryGrid.allowOverflow(true);
        //player inventory
        int col = 0;
        int row = 0;
        for(int i = 6; i < this.handler.slots.size(); i++){
            FlowLayout slotComponentContainer = buildSlot(i);
            inventoryGrid.child(slotComponentContainer, row, col);
            col++;
            if(col > 8){
                col = 0;
                row++;
            }
        }

        return inventoryGrid;
    }

    @Override
    public void close() {
        PetPartyUpdateNotifier.getInstance().unsubscribe(sub);
        super.close();
    }
}

/*

Problem: I need to get the progression data from the entity to display it here

My current approach: use the PetOwnerComponent to get to the PetMetadataComponent.

    - problem: i need the entity to get the PetMetadataComponent. The entity is not available in
    the client.

 */
