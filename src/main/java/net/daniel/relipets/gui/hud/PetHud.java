package net.daniel.relipets.gui.hud;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.items.Petificator;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

public class PetHud {

    public static boolean shouldRender(){
        if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getMainHandStack() != null){
            return MinecraftClient.getInstance().player.getMainHandStack().getItem() instanceof Petificator;
        }

        return false;
    }

    static int verticalSpacing = 8;
    static int leftOffset = 20;
    static int baseHealthBarSize = 60;
    static int baseHealthBarHeight = 6;
    static int bottomOffset = 42;
    static int slotWidth = 10;
    static int slotHeight = 10;
    static int slotContentPadding = 3;
    static int highLightMargin = 2;

    public static void renderCallback(DrawContext drawContext, float tickDelta){

        if(!shouldRender()) return;

        //flow: top to bottom

        //set bottom offset (manually adjust this to move things "up" as the menu grows)

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        int height = client.getWindow().getScaledHeight();
        int width = client.getWindow().getScaledWidth();
        int currentYPos = height - bottomOffset;


        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);//shouldRender already handles player being null
        PetData selectedPet = petOwner.getPetParty().getSelectedPet();

        if(selectedPet != null){
            String summonState = selectedPet.getSummonState();
            if(summonState.equals(PetData.HEALING)){
                summonState += " " + String.format(" %.2fs", Utils.tickToSecond(selectedPet.getHealingCooldown()));
            }
            String selectedPetName = selectedPet.getPetInfo().getPetName() + " (" + summonState + ")";
            //draw the selected pet name
            drawContext.drawText(client.textRenderer, selectedPetName, leftOffset, currentYPos, 0xcccccc,false);

            currentYPos += 5; //text size

            currentYPos += verticalSpacing;

            //draw pet health
            float currentHealthPercent = (float) selectedPet.getPetInfo().getCurrentHealth() / selectedPet.getPetInfo().getMaxHealth();
            //health bar bg
            drawContext.fill(leftOffset,currentYPos, leftOffset + baseHealthBarSize,currentYPos + baseHealthBarHeight, 0xaaaaaaaa);
            //pet current health
            drawContext.fill(leftOffset,currentYPos, (int) (leftOffset + (baseHealthBarSize * currentHealthPercent)),currentYPos + baseHealthBarHeight, 0xffaa0000);

            currentYPos += baseHealthBarHeight;

            currentYPos += verticalSpacing;
        }else{
            currentYPos += 5; //text size

            currentYPos += verticalSpacing;

            currentYPos += baseHealthBarHeight;

            currentYPos += verticalSpacing;
        }

        int currentSlotOffset = leftOffset;
        for(int i = 0; i < petOwner.getPetParty().getSlotManager().getSlots().size(); i++){

            if(petOwner.getPetParty().getSelectedPetIndex() == i){
                drawContext.fill(currentSlotOffset - highLightMargin, currentYPos-highLightMargin, currentSlotOffset + slotWidth + highLightMargin, currentYPos + slotHeight + highLightMargin, 0xff2AD4CC);
            }

            drawContext.fill(currentSlotOffset, currentYPos, currentSlotOffset + slotWidth, currentYPos + slotHeight, 0xffeeeeee);

            if(!petOwner.getPetParty().getSlotManager().getSlotAt(i).isEmpty()){
                drawContext.fill(currentSlotOffset + slotContentPadding, currentYPos+slotContentPadding, currentSlotOffset + slotWidth - slotContentPadding, currentYPos + slotHeight - slotContentPadding, 0xff333333);
            }

            currentSlotOffset += slotWidth;
            currentSlotOffset += 4;
        }
        //draw pet slots
        //  draw the currently selected slot with an accent

        //drawContext.fill(0, 0, 100, 100, 0, 0xFFFF0000);

    }

}
