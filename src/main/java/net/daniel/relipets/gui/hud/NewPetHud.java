package net.daniel.relipets.gui.hud;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.items.Petificator;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class NewPetHud {

    public static final int verticalSpacing = 2;

    private static final Identifier HUD_ID = new Identifier(Relipets.MOD_ID, "hud-id");
    private static final Identifier RED_HEALTH_BAR_ID = new Identifier(Relipets.MOD_ID, "red-health-bar");
    private static final Identifier GRAY_HEALTH_BAR_ID = new Identifier(Relipets.MOD_ID, "gray-health-bar");
    private static final Identifier HEALTH_BAR_CONTAINER_ID = new Identifier(Relipets.MOD_ID, "health-bar-container");
    private static final Identifier XP_BAR_CONTAINER_ID = new Identifier(Relipets.MOD_ID, "xp-bar-container");
    private static final Identifier ENTITY_NAME_LABEL_ID = new Identifier(Relipets.MOD_ID, "entity-name-label");

    private static FlowLayout hudLayout;
    private static LabelComponent entityNameLabel;

    private static FlowLayout healthBarContainer;
    private static BoxComponent healthBarRed;
    private static BoxComponent healthBarGray;

    private static FlowLayout xpBarContainer;
    private static BoxComponent xpBarWhite;
    private static BoxComponent xpBarGray;

    private static ScrollContainer<FlowLayout> slotsLayout;
    private static boolean initialized = false;
    private static int selectedPetSlot = 0;
    private static int slotsVisisble = 3;

    static int bottomOffset = 42;
    static int slotSize = 22;
    private static boolean builtSlots = false;
    static int slotSpacing = 4;
    private static FlowLayout slotsContainer;
    private static boolean addedLayout = false;
    private static int slotsSize;
    private static boolean scrollToSelectedPetNextRenderPass;
    private static LabelComponent levelLabel;

    public static boolean shouldRender(){
        if(MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getMainHandStack() != null){
            return MinecraftClient.getInstance().player.getMainHandStack().getItem() instanceof Petificator;
        }

        return false;
    }

    public static void init(){
        if(!Hud.hasComponent(HUD_ID)){
            addedLayout = true;

            hudLayout = Containers.verticalFlow(Sizing.content(5), Sizing.content(5));

            //add label showing the entity name
            entityNameLabel = Components.label(Text.of("Iron Golem"))
                    .color(Color.ofRgb(0xcccccc));

            entityNameLabel.margins(Insets.bottom(verticalSpacing));
            entityNameLabel.id(ENTITY_NAME_LABEL_ID.toString());

            hudLayout.child(entityNameLabel);

            //health bar
            healthBarRed = Components.box(Sizing.fill(60), Sizing.fixed(6)).fill(true)
                    .color(Color.ofArgb(0xffaa0000));

            healthBarGray = Components.box(Sizing.fill(40), Sizing.fixed(6)).fill(true)
                    .color(Color.ofArgb(0xaaaaaaaa));

            slotsSize = (slotSize + slotSpacing) * slotsVisisble;

            healthBarContainer = Containers.horizontalFlow(Sizing.fixed(slotsSize), Sizing.fixed(6))
                    .child(healthBarRed)
                    .child(healthBarGray);

            healthBarContainer.id(HEALTH_BAR_CONTAINER_ID.toString()).margins(Insets.bottom(verticalSpacing));

            hudLayout.child(healthBarContainer);

            //xp bar
            xpBarContainer = Containers.horizontalFlow(Sizing.fixed(slotsSize), Sizing.fixed(9));
            xpBarContainer.margins(Insets.bottom(3));
            xpBarContainer.verticalAlignment(VerticalAlignment.CENTER);
            xpBarContainer.id(XP_BAR_CONTAINER_ID.toString());

            xpBarWhite = Components.box(Sizing.fill(0), Sizing.fixed(6)).fill(true)
                    .color(Color.ofArgb(0xffaaaaaa));

            xpBarGray = Components.box(Sizing.fill(100), Sizing.fixed(6)).fill(true)
                    .color(Color.ofArgb(0xaaaaaaaa));

            levelLabel = Components.label(Text.literal("0"));

            levelLabel.margins(Insets.left(5));

            xpBarContainer.child(xpBarWhite);
            xpBarContainer.child(xpBarGray);
            xpBarContainer.child(levelLabel);
            hudLayout.child(xpBarContainer);

            //slots
            slotsContainer = Containers.horizontalFlow(Sizing.content(), Sizing.content());

            slotsLayout = Containers.horizontalScroll(Sizing.fixed(slotsSize), Sizing.content(), slotsContainer);

            hudLayout.child(slotsLayout);

            Hud.add(HUD_ID, ()-> hudLayout);
        }
    }

    public static void tick(DrawContext context, float v){

        if(shouldRender()){
            //add hud layout if not present
            init();
        }else{
            //remove hud layout if present
            if(Hud.hasComponent(HUD_ID)){
                Hud.remove(HUD_ID);
            }
            return;
        }


        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        int height = client.getWindow().getScaledHeight();
        int width = client.getWindow().getScaledWidth();

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);//shouldRender already handles player being null
        PetData selectedPet = petOwner.getPetParty().getSelectedPet();

        buildSlotsIfNeeded(petOwner, petOwner.getPetParty().getSelectedPetIndex());

        if(selectedPet != null){

            addBarsAndLabel();

            drawCurrentSelectedPetLabel(selectedPet, null);

            drawHealthBar(selectedPet, null);

            drawXpBar(selectedPet, null);

        }else{
            drawCurrentSelectedPetLabel(null, "Empty");
            drawHealthBar(null, 0);
            drawXpBar(null, 0);
        }

        //This is here to fix the fact that you cant add the components and scroll to them at the same time.
        //With this the hud scrolls to the selected pet when you equip the wand. Very handy.
        if(scrollToSelectedPetNextRenderPass){
            scrollToSelectedPet(petOwner);
            scrollToSelectedPetNextRenderPass = false;
        }

        //set the correct position of the hud
        if(addedLayout){
            hudLayout.positioning(Positioning.absolute(width - 120, height - 60));
            addedLayout = false;
            scrollToSelectedPetNextRenderPass = true;
        }

    }

    private static void scrollToSelectedPet(PetOwnerComponent petOwner){
        int currentSlotCount = petOwner.getPetParty().getSlotManager().getSlots().size();
        float percent = ((float) (slotSize * selectedPetSlot) / ((currentSlotCount-1) * slotSize));
        slotsLayout.scrollTo(percent);
    }

    private static void buildSlotsIfNeeded(PetOwnerComponent petOwner, int actualSelectedPetSlot) {
        int currentSlotCount = petOwner.getPetParty().getSlotManager().getSlots().size();
        if(addedLayout || (selectedPetSlot != actualSelectedPetSlot) || slotsLayout.child().children().size() != currentSlotCount){
            selectedPetSlot = actualSelectedPetSlot;
            slotsLayout.child().clearChildren();

            for(int i = 0; i < currentSlotCount; i++){
                Surface slotSurface = Surface.VANILLA_TRANSLUCENT;

                if(i == selectedPetSlot){
                    slotSurface = Surface.PANEL;
                }

                FlowLayout slotContainer = Containers.verticalFlow(Sizing.fixed(slotSize), Sizing.fixed(slotSize));
                slotContainer.surface(slotSurface);
                        //.margins(Insets.right(slotSpacing));

                slotContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

                PetData petData = petOwner.getPetParty().getSlotManager().getSlotAt(i).getContent();

                if(petData != null && petData.getPetEntityData().isValid()){
                    Identifier entityTypeId = new Identifier(petData.getPetEntityData().getEntityType());

                    EntityType<LivingEntity> entityType = (EntityType<LivingEntity>) Registries.ENTITY_TYPE.get(entityTypeId);

                    slotContainer.child(
                            Components.entity(Sizing.fixed(slotSize), entityType, petData.getPetEntityData().getEntityNbt())
                                    .scaleToFit(true)
                    );
                }

                slotsLayout.child().child(
                   slotContainer
                );

            }

            scrollToSelectedPet(petOwner);

        }

    }

    private static void drawCurrentSelectedPetLabel(PetData selectedPet, @Nullable String labelOverride){
        if(labelOverride != null){
            entityNameLabel.text(Text.of(labelOverride));
        }else{
            String summonState = selectedPet.getSummonState();
            if(summonState.equals(PetData.HEALING)){
                summonState += " " + String.format(" %.1fs", Utils.tickToSecond(selectedPet.getHealingCooldown()));
            }
            String selectedPetName = selectedPet.getPetInfo().getPetName() + " (" + summonState + ")";
            entityNameLabel.text(Text.of(selectedPetName));


        }

    }

    private static void drawHealthBar(PetData selectedPet, @Nullable Integer healthOverride){
        int currentHealthPercent;

        if(healthOverride == null){
            currentHealthPercent = (int)(100 * (float) selectedPet.getPetInfo().getCurrentHealth() / selectedPet.getPetInfo().getMaxHealth());
        }else{
            currentHealthPercent = healthOverride;
        }


        healthBarRed.sizing(Sizing.fill(currentHealthPercent), Sizing.fixed(6));
        healthBarGray.sizing(Sizing.fill(100 - currentHealthPercent), Sizing.fixed(6));
    }

    private static void drawXpBar(PetData selectedPet, @Nullable Integer xpOverride){
        int currentXpPercent = 0;
        String text = "0";
        if(xpOverride == null){

            currentXpPercent = (int)(80 * (float) selectedPet.getPetInfo().getLevelProgression().getCurrentXp() /
                    selectedPet.getPetInfo().getLevelProgression().getXpRequiredForNextLevel());

            text = String.valueOf(selectedPet.getPetInfo().getLevelProgression().getCurrentLevel());

        }else{
            currentXpPercent = xpOverride;
        }


        xpBarWhite.sizing(Sizing.fill(currentXpPercent), Sizing.fixed(6));
        xpBarGray.sizing(Sizing.fill(80 - currentXpPercent), Sizing.fixed(6));
        levelLabel.text(Text.literal(text));
    }

    private static void addBarsAndLabel(){
        if(hudLayout.childById(LabelComponent.class, ENTITY_NAME_LABEL_ID.toString()) == null){
            hudLayout.child(0, entityNameLabel);
        }

        if(hudLayout.childById(FlowLayout.class, HEALTH_BAR_CONTAINER_ID.toString()) == null){
            hudLayout.child(1, healthBarContainer);
        }

        if(hudLayout.childById(FlowLayout.class, XP_BAR_CONTAINER_ID.toString()) == null){
            hudLayout.child(2, xpBarContainer);
        }
    }

    private static void removeHealthAndPetLabelIfPresent(){
        if(hudLayout.childById(LabelComponent.class, ENTITY_NAME_LABEL_ID.toString()) != null){
            hudLayout.removeChild(entityNameLabel);
        }

        if(hudLayout.childById(FlowLayout.class, HEALTH_BAR_CONTAINER_ID.toString()) != null){
            hudLayout.removeChild(healthBarContainer);
        }
    }

}
