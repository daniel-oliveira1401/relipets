package net.daniel.relipets.gui.screen;

import net.daniel.relipets.cca_components.PartSystem;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetInventoryManager;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.GuiRelatedStuffRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;

public class PartManagementScreenHandler extends ScreenHandler {
    private final PetInventoryManager inventoryManager;
    ArrayPropertyDelegate properties;
    PlayerEntity player;
    public PartManagementScreenHandler(int syncId, PlayerInventory playerInventory, PlayerEntity player, PetData selectedPetData, int selectedPetSlot) {
        super(GuiRelatedStuffRegistry.PART_MANAGEMENT_SCREEN_HANDLER_TYPE, syncId);
        this.player = player;

        if(player.getWorld().isClient()){
            this.inventoryManager = new PetInventoryManager();
        }
        else{
            this.inventoryManager = selectedPetData.loadPartSystemIntoMemory().getPetInventoryManager();

            PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            PetData petData = petOwnerComponent.getPetParty().getSlotManager().getSlotAt(selectedPetSlot).getContent();
            if(petData != null){
                petData.syncItemsWithPartSystem(player.getServer());
                petOwnerComponent.getPetParty().pushChangesToClient();
            }

            this.addListener(new ScreenHandlerListener() {
                @Override
                public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
                    if(slotId < 6 && selectedPetSlot != -1){
                        PetOwnerComponent petOwnerComponent = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                        PetData petData = petOwnerComponent.getPetParty().getSlotManager().getSlotAt(selectedPetSlot).getContent();
                        if(petData != null){
                            petData.applySlotContentChange(slotId, player.getServer());
                            petOwnerComponent.getPetParty().pushChangesToClient();
                        }
                    }
                }

                @Override
                public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

                }
            });
        }

        final int slotSize = 18;
        final int slotSpacing = 8;
        for (int i = 0; i < this.inventoryManager.size(); i++) {
            this.addSlot(new Slot(this.inventoryManager, i, 0, 0){
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.getItem() instanceof PartItem && stack.getItem().toString().contains(PartSystem.availableParts[this.getIndex()]);
                }
            }); // Positions are handled by owo-ui
        }


        // Player main inventory (3 rows of 9)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, slotSpacing + col * slotSize, 84 + row * slotSize));
            }
        }

        // Player hotbar (1 row of 9)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, slotSpacing + i * slotSize, 142));
        }
        this.properties = new ArrayPropertyDelegate(1);
        this.addProperties(this.properties);
        this.setPetSlot(selectedPetSlot);

    }

    public PartManagementScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, playerInventory.player,
                null,
                -1);

    }

    public int getPetSlot(){
        return this.properties.get(0);
    }

    public void setPetSlot(int slot){
        this.properties.set(0, slot);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getStack();
        ItemStack stackCopy = originalStack.copy();

        int petSlotCount = inventoryManager.size();

        if (index < petSlotCount) {
            // Shift-clicking from pet inventory → move to player inventory
            if (!this.insertItem(originalStack, petSlotCount, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Shift-clicking from player inventory → move to pet inventory
            if (!this.insertItem(originalStack, 0, petSlotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (originalStack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stackCopy;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }




}
