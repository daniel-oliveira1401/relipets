package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetGroup;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.cca_components.pet_management.PetSlot;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PetGroupsScreen extends BaseOwoScreen<FlowLayout> {

    static int slotSize = 22;
    static int groupBarSize = 6;
    BaseOwoScreen<FlowLayout> parent;
    private FlowLayout rootComponent;
    private int rows;
    private GridLayout rightPaneContainer;
    private PetPartyUpdateNotifier.Subscriber sub;
    private FlowLayout groupsContainer;
    private FlowLayout leftPaneContainer;
    private OverlayContainer<GridLayout> addSlotToGroupOverlay;
    private OverlayContainer<GridLayout> removeSlotFromGroupOverlay;

    int disabledSlotColor = 0xffcccccc;

    public PetGroupsScreen(BaseOwoScreen<FlowLayout> parent){
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

    @Override
    protected void build(FlowLayout rootComponent) {
        if(this.client == null || this.client.player == null) return;
        //ColorPickerComponent picker = new ColorPickerComponent();
        this.sub = (p)-> {
            this.rootComponent.queue(()-> {
                onPartyUpdated(p);
            });
        };

        PetPartyUpdateNotifier.getInstance().subscribe(sub);

        this.rootComponent = rootComponent;
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.padding(Insets.both(15, 15));

        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        //rootComponent.padding(Insets.both(15, 15));

        ButtonComponent backBtn = Components.button(Text.of("< Back"), this::backToMainScreen);
        backBtn.margins(Insets.bottom(10));
        rootComponent.child(backBtn);

        var bodyContainer = Containers.horizontalFlow(Sizing.fill(100), Sizing.fill(80));
        bodyContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();

        //this is the container of the left pane
        this.leftPaneContainer = Containers.verticalFlow(Sizing.fixed(300), Sizing.fill(100));
        leftPaneContainer.padding(Insets.both(5, 5));
        //leftPaneContainer.surface(Surface.flat(0xffff0000));

        buildLeftPanel(party);

        //display all pets in a grid to the right
        this.rows = (int) Math.ceil((double) party.getSlotManager().getSlotCount() / columnCount);

        this.rightPaneContainer = Containers.grid(Sizing.content(), Sizing.content(), rows, columnCount);
        //rightPaneContainer.padding(Insets.both(2, 2));
        rightPaneContainer.surface(Surface.PANEL_INSET);
        rightPaneContainer.id("grid");

        buildRightPanel();

        bodyContainer.child(rightPaneContainer);

        bodyContainer.child(0, leftPaneContainer);

        rootComponent.child(bodyContainer);

        onPartyUpdated(party);

    }

    public void buildLeftPanel(PetParty party){
        //add the top bar with the option to create a new group
        leftPaneContainer.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).child(
                        Components.label(Text.of("Pet Groups")).margins(Insets.right(5))
                ).child(
                        Components.button(Text.of("+ Add Group"), (b)-> addGroup())
                ).margins(Insets.bottom(5)).alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)

        );

        //add the groups
        this.groupsContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        buildGroups(party);

        leftPaneContainer.child(
                Containers.verticalScroll(Sizing.fill(100), Sizing.fill(80), this.groupsContainer)
                        .scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE)).padding(Insets.of(5))
        );
    }

    private void buildGroups(PetParty party){
        this.groupsContainer.clearChildren();

        if(party.getPetGroupManager() != null){
            party.getPetGroupManager().getGroups().forEach((group)-> {
                FlowLayout groupComponent = buildGroup(group, party);
                groupsContainer.child(groupComponent);
            });
        }
    }

    private FlowLayout buildGroup(PetGroup group, PetParty party) {
        var verticalSpacing = 5;
        FlowLayout groupContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        groupContainer.surface(Surface.outline(0xffeeeeee));
        groupContainer.margins(Insets.bottom(5));
        BoxComponent colorBox = Components.box(Sizing.fixed(20), Sizing.fixed(20))
                .color(Color.ofArgb(group.getColor())).fill(true);
        colorBox.margins(Insets.right(5));
        colorBox.mouseDown().subscribe((a, b, c) -> openColorPicker(group));

        TextBoxComponent groupName = Components.textBox(Sizing.fixed(120), group.getName());
        groupName.onChanged().subscribe((name) -> setGroupName(group, name));

        groupContainer
                //title
                .child(
                        Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                                .child(
                                        Containers.horizontalFlow(Sizing.content(), Sizing.content()).child(
                                                Components.button(Text.of("X"), (b)-> removeGroup(group)).margins(Insets.right(5))
                                        ).child(
                                                colorBox
                                        ).child(
                                                groupName
                                        ).verticalAlignment(VerticalAlignment.CENTER)
                                ).child(
                                        Containers.horizontalFlow(Sizing.fixed(100),Sizing.content()).child(
                                                Components.label(Text.of("Slots")).margins(Insets.right(5))
                                        ).child(
                                                Components.button(Text.of("+"), (b) -> openSelectSlotToAddModal(group))
                                        ).child(
                                                Components.button(Text.of("-"), (b) -> openSelectSlotToRemoveModal(group))
                                        ).alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER)
                                ).verticalAlignment(VerticalAlignment.CENTER).padding(Insets.of(verticalSpacing))

                );

        FlowLayout slotsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        //build the slots to display in this group
        for(int slot : group.getSlots()){
            FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize));
            slotContainer.margins(Insets.both(5, 5));

            slotContainer.surface(Surface.outline(group.getColor()));

            slotContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            PetData petData = party.getSlotManager().getSlotAt(slot).getContent();

            if(petData != null && petData.getPetEntityData().isValid()){
                Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

                EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

                EntityComponent component = Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
                        .scaleToFit(true);

                slotContainer.child(
                        component
                );
            }

            slotsContainer.child(slotContainer);
        }

        groupContainer.child(
                Containers.horizontalFlow(Sizing.fill(100), Sizing.content()).child(
                    Containers.horizontalScroll(Sizing.fill(65), Sizing.content(), slotsContainer).scrollbar(ScrollContainer.Scrollbar.flat(Color.WHITE)).padding(Insets.of(5))
                ).child(
                        Containers.horizontalFlow(Sizing.fill(35), Sizing.content()).child(
                                Components.button(Text.of("Summon"), (b)-> this.summonGroup(group))
                        ).child(
                                Components.button(Text.of("Recall"), (b)-> this.recallGroup(group))
                        )

                ).verticalAlignment(VerticalAlignment.CENTER)
        );

        return groupContainer;

    }

    private void recallGroup(PetGroup group) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        ClientPlayNetworking.send(C2SPacketHandlers.RECALL_GROUP, buf);
    }

    private void summonGroup(PetGroup group) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        ClientPlayNetworking.send(C2SPacketHandlers.SUMMON_GROUP, buf);
    }

    private boolean openColorPicker(PetGroup group) {
        System.out.println("Open color picker :)");
        ColorPickerComponent colorPicker = new ColorPickerComponent();
        colorPicker.selectedColor(Color.ofArgb(group.getColor()));
        colorPicker.onChanged().subscribe((color)-> setGroupColor(group, color));
        colorPicker.sizing(Sizing.fixed(100));
        colorPicker.showAlpha(true);

        this.rootComponent.child(
            Containers.overlay(
                colorPicker
            ).zIndex(99)

        );

        return true;
    }

    private void setGroupColor(PetGroup group, Color color) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        buf.writeInt(color.argb());
        ClientPlayNetworking.send(C2SPacketHandlers.CHANGE_GROUP_COLOR, buf);
    }

    private void setGroupName(PetGroup group, String name) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        buf.writeString(name);
        ClientPlayNetworking.send(C2SPacketHandlers.CHANGE_GROUP_NAME, buf);
    }

    private void removeGroup(PetGroup group) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        ClientPlayNetworking.send(C2SPacketHandlers.REMOVE_GROUP, buf);

    }

    private void openSelectSlotToRemoveModal(PetGroup group) {
        //build a grid with the slots
        GridLayout grid = buildSlotsToRemoveGrid(group);
        if(grid != null){
            grid.padding(Insets.of(10));
            grid.surface(Surface.PANEL_INSET);

            this.removeSlotFromGroupOverlay = Containers.overlay(grid);
            this.removeSlotFromGroupOverlay.zIndex(99);

            this.rootComponent.child(
                    this.removeSlotFromGroupOverlay
            );

        }
    }

    private void openSelectSlotToAddModal(PetGroup group) {

        //build a grid with the slots
        GridLayout grid = buildSlotsToAddGrid(group);
        if(grid != null){
            grid.padding(Insets.of(10));
            grid.surface(Surface.PANEL_INSET);

            this.addSlotToGroupOverlay = Containers.overlay(grid);
            this.addSlotToGroupOverlay.zIndex(99);

            this.rootComponent.child(
                this.addSlotToGroupOverlay
            );

        }
        //when a slot is clicked, add that slot
    }

    public void buildRightPanel(){
        if(this.client == null || this.client.player == null || this.rightPaneContainer == null) return;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();

        for(int i = 0; i <party.getSlotManager().getSlotCount(); i++){

            int currentRow = (int) Math.floor((double)i / columnCount);

            int currentColumn = i - currentRow * columnCount;

            Surface slotSurface = Surface.VANILLA_TRANSLUCENT;

            FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize + groupBarSize));
            slotContainer.margins(Insets.both(5, 5));
            slotContainer.surface(slotSurface);
            int finalI = i;
            slotContainer.mouseDown().subscribe((a, b, c)-> this.onSlotClicked(finalI));
            //.margins(Insets.right(slotSpacing));

            slotContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM);

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

            slotContainer.child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(groupBarSize)).id("groupBar")
            );

            this.rightPaneContainer.child(slotContainer, currentRow, currentColumn);
        }

    }

    @Nullable
    public GridLayout buildSlotsToAddGrid(PetGroup group){

        if(this.client == null || this.client.player == null || this.rightPaneContainer == null) return null;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();

        this.rows = (int) Math.ceil((double) party.getSlotManager().getSlots().size() / columnCount);

        GridLayout grid = Containers.grid(Sizing.content(), Sizing.content(), rows, 5);

        for(int i = 0; i <party.getSlotManager().getSlotCount(); i++){

            int currentRow = (int) Math.floor((double)i / columnCount);

            int currentColumn = i - currentRow * columnCount;

            Surface slotSurface = Surface.VANILLA_TRANSLUCENT;

            FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize));
            slotContainer.margins(Insets.both(5, 5));

            if(group.getSlots().contains(Integer.valueOf(i))){
                slotSurface = Surface.flat(disabledSlotColor);
            }else{
                int finalI = i;
                slotContainer.mouseDown().subscribe((a, b, c)-> this.addSlotToGroup(group, finalI));
            }

            slotContainer.surface(slotSurface);

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

            grid.child(slotContainer, currentRow, currentColumn);
        }

        return grid;
    }

    @Nullable
    public GridLayout buildSlotsToRemoveGrid(PetGroup group){

        if(this.client == null || this.client.player == null || this.rightPaneContainer == null) return null;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetParty party = petOwner.getPetParty();

        this.rows = (int) Math.ceil((double) party.getSlotManager().getSlots().size() / columnCount);

        GridLayout grid = Containers.grid(Sizing.content(), Sizing.content(), rows, 5);

        for(int i = 0; i <party.getSlotManager().getSlotCount(); i++){

            int currentRow = (int) Math.floor((double)i / columnCount);

            int currentColumn = i - currentRow * columnCount;

            Surface slotSurface = Surface.VANILLA_TRANSLUCENT;

            FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize));
            slotContainer.margins(Insets.both(5, 5));


            if(!group.getSlots().contains(Integer.valueOf(i))){
                slotSurface = Surface.flat(this.disabledSlotColor);
            }else{
                int finalI = i;
                slotContainer.mouseDown().subscribe((a, b, c)-> this.removeSlotFromGroup(group, finalI));
            }

            slotContainer.surface(slotSurface);

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

            grid.child(slotContainer, currentRow, currentColumn);
        }

        return grid;
    }


    private boolean addSlotToGroup(PetGroup group, int slotIndex) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        buf.writeInt(slotIndex);
        ClientPlayNetworking.send(C2SPacketHandlers.ADD_SLOT_TO_GROUP, buf);
        this.addSlotToGroupOverlay.remove();
        return true;
    }

    private boolean removeSlotFromGroup(PetGroup group, int slotIndex){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(group.getId().toString());
        buf.writeInt(slotIndex);
        ClientPlayNetworking.send(C2SPacketHandlers.REMOVE_SLOT_FROM_GROUP, buf);
        this.removeSlotFromGroupOverlay.remove();
        return true;
    }

    public void onPartyUpdated(PetParty party){
        if(this.client == null || this.client.player == null || this.rightPaneContainer == null) return;

        // ============== Update the slots for the right panel ============
        for(int i = 0; i < this.rightPaneContainer.children().size(); i++){

            FlowLayout slotContainer = (FlowLayout) this.rightPaneContainer.children().get(i);
            //get the group that this slot belongs to (if any)


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

            if(party.getPetGroupManager() != null){
                List<PetGroup> groups = party.getPetGroupManager().getSlotGroups(i);
                if(groups != null && !groups.isEmpty()){

                    FlowLayout groupBar = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(groupBarSize));

                    if(groupBar != null){

                        groupBar.clearChildren();
                        groups.forEach((g)-> {
                            int color = g.getColor();
                            BoxComponent bgBox = Components.box(Sizing.fill((int)Math.floor(100.0f/ groups.size())), Sizing.fill(100));
                            bgBox.fill(true);
                            bgBox.color(Color.ofArgb(color));
                            groupBar.child(bgBox);
                        });

                        slotContainer.child(
                                groupBar
                        );

                    }

                }
            }

        }

        //============== update the left panel ==========
        buildGroups(party);
    }

    int columnCount = 3;

    public void addGroup(){
        ClientPlayNetworking.send(C2SPacketHandlers.CREATE_GROUP, PacketByteBufs.empty());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        try{

            super.render(context, mouseX, mouseY, delta);
        }catch (Exception e){
            System.out.println("Owo lib exploded");
        }
        //iterate over the slots
        //  if the slot index matches the selected slot, then paint it with another color

    }



    int selectedSlot = -1;

    public boolean onSlotClicked(int slotIndex){
        return true;
        //check if there is a slot selected
        //if there isnt, then select one
//        if(this.selectedSlot == slotIndex){
//            this.selectedSlot = -1;
//        }else if(this.selectedSlot == -1){
//            this.selectedSlot = slotIndex;
//        }else {
//            PacketByteBuf buf = PacketByteBufs.create();
//            buf.writeInt(this.selectedSlot); //origin
//            buf.writeInt(slotIndex); //destination
//            ClientPlayNetworking.send(C2SPacketHandlers.REORDER_PETS, buf);
//            this.selectedSlot = -1;
//        }
//
//        if(this.rootComponent != null){
//            if(rightPaneContainer != null){
//
//                for(int i = 0; i < rightPaneContainer.children().size(); i++){
//
//                    var slot = rightPaneContainer.children().get(i);
//                    if(slot instanceof FlowLayout flowSlot){
//                        flowSlot.surface(Surface.VANILLA_TRANSLUCENT);
//                        if(i == this.selectedSlot){
//                            flowSlot.surface(Surface.PANEL);
//                        }
//                    }
//
//                }
//
//
//            }
//        }
//
//        return true;
    }

    private void backToMainScreen(ButtonComponent btn){
        if(this.client != null){
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void removed() {
        super.removed();
        PetPartyUpdateNotifier.getInstance().unsubscribe(this.sub);
    }
}
