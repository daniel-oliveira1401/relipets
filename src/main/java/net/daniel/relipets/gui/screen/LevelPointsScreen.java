package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.progression.StatsEnum;
import net.daniel.relipets.cca_components.pet_management.progression.StatsOperationEnum;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LevelPointsScreen extends BaseOwoScreen<FlowLayout> {

    String statCurrentValueSuffix = "_current_value";

    BaseOwoScreen<?> parent;
    private FlowLayout bodyContainer;
    private @Nullable LabelComponent healthStatLabel;
    private @Nullable LabelComponent healthCurrentStatValue;
    private LabelComponent pointsRemaining;

    FlowLayout rootComponent;

    public LevelPointsScreen(@NotNull BaseOwoScreen<?> parent){
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
        this.rootComponent = rootComponent;

        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        rootComponent.padding(Insets.both(15, 15));

        ButtonComponent backBtn = Components.button(Text.of("< Back"), this::backToMainScreen);
        backBtn.margins(Insets.bottom(10));
        rootComponent.child(backBtn);

        bodyContainer = Containers.verticalFlow(Sizing.fill(100), Sizing.content(100));
        bodyContainer.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        if(this.client == null || this.client.player == null) return;

        PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
        PetData selectedPet = petOwner.getPetParty().getSelectedPet();

        if(selectedPet != null && selectedPet.isSummonedNoEntityValidation()){

            PetMetadataComponent petMetadata = selectedPet.getPetEntityData().getMetadata(this.client.player.getWorld());
            if(petMetadata != null){

                buildParams(rootComponent, petMetadata, selectedPet);

                buildPointsLeftLabel();

            }else{
                bodyContainer.child(Components.label(Text.of("Could not find metadata for selected pet")));
            }
        }else{
            bodyContainer.child(Components.label(Text.of("A pet must be selected and summoned to change their stats")));
        }

        rootComponent.child(bodyContainer);


    }

    private void buildPointsLeftLabel() {
        this.pointsRemaining = Components.label(Text.of("0"));
        this.pointsRemaining.margins(Insets.top(10));
        this.pointsRemaining.sizing(Sizing.fill(100), Sizing.content());
        this.pointsRemaining.horizontalTextAlignment(HorizontalAlignment.CENTER);
        this.bodyContainer.child(pointsRemaining);
    }

    public void buildParams(FlowLayout rootComponent, PetMetadataComponent petMetadata, PetData selectedPet){

        rootComponent.child(Components.label(Text.literal("Level Points for "+ selectedPet.getPetInfo().getPetName())));


        petMetadata.getStatUpgrades().getStats().keySet().forEach((stat)-> {
            if(this.client != null && this.client.player != null){

                if(selectedPet.getPetEntityData().entityHasStat(this.client.player.getWorld(), stat)){

                    FlowLayout healthStat = createParam(formatStatName(stat.name()),
                            petMetadata.getStatUpgrades().getStatValue(stat),
                            stat);


                    bodyContainer.child(healthStat);
                }


            }

        });

    }

    public String formatStatName(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Split by underscores, convert first letter of each word to uppercase, rest to lowercase
        String[] words = input.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0))) // Capitalize first letter
                        .append(word.substring(1).toLowerCase()) // Lowercase the rest
                        .append(" "); // Add space
            }
        }

        return formatted.toString().trim(); // Remove trailing space
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(!this.invalid && this.client != null && this.client.player != null){
            //update the contents of the screen that need to be updated
            PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(this.client.player);
            PetData selectedPet = petOwner.getPetParty().getSelectedPet();

            if(selectedPet != null && selectedPet.isSummonedNoEntityValidation()){

                PetMetadataComponent petMetadata = selectedPet.getPetEntityData().getMetadata(this.client.player.getWorld());

                if(petMetadata != null){

                    //updateHealthStat(petMetadata, selectedPet);
                    updateStats(this.rootComponent, petMetadata, selectedPet);

                    updateTotalPointsUsed(petMetadata);

                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void updateTotalPointsUsed(PetMetadataComponent petMetadata) {
        if(this.pointsRemaining != null){
            int totalPoints = petMetadata.getLevelProgression().getCurrentLevel();
            int totalPointsUsed = petMetadata.getStatUpgrades().getTotalPointsUsed();

            String pointsText = "Total points used: " + totalPointsUsed + "/" + totalPoints;

            this.pointsRemaining.text(Text.of(pointsText));
        }
    }

    private void updateStats(FlowLayout rootComponent, PetMetadataComponent petMetadata, PetData selectedPet){
        petMetadata.getStatUpgrades().getStats().forEach((stat, statValue)-> {
            LabelComponent statValueLabel = rootComponent.childById(LabelComponent.class, formatStatName(stat.name()));

            if(statValueLabel != null)
                statValueLabel.text(Text.of(String.valueOf(petMetadata.getStatUpgrades().getStatValue(stat))));

            LabelComponent statCurrentValue = rootComponent.childById(LabelComponent.class, formatStatName(stat.name()) + statCurrentValueSuffix);
            if(statCurrentValue != null)
                statCurrentValue.text(Text.of(formatDisplayValue(petMetadata.getCurrentValueByStatName(stat, selectedPet))));
        });
    }

    public String formatDisplayValue(float statValue){
        return String.format("%.2f", statValue);
    }

    private void backToMainScreen(ButtonComponent btn){
        if(this.client != null){
            this.client.setScreen(this.parent);
        }
    }

    private FlowLayout createParam(String paramName, int pValue, StatsEnum stat){
        FlowLayout container = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        container.verticalAlignment(VerticalAlignment.CENTER);

        LabelComponent paramLabel = Components.label(Text.of(paramName));
        paramLabel.sizing(Sizing.fixed(120), Sizing.content());
        container.child(paramLabel);

        LabelComponent paramValue = Components.label(Text.of(String.valueOf(pValue)));
        paramValue.sizing(Sizing.fixed(30), Sizing.content());
        paramValue.id(paramName);
        container.child(paramValue);

        ButtonComponent btnDec = Components.button(Text.of("-"), (b)-> changeStatPoint(StatsOperationEnum.DECREASE, stat));
        container.child(btnDec);

        ButtonComponent btnInc = Components.button(Text.of("+"), (b)-> changeStatPoint(StatsOperationEnum.INCREASE, stat));
        btnInc.margins(Insets.right(4));
        container.child(btnInc);


        LabelComponent currentStatValue = Components.label(Text.of(""));
        currentStatValue.sizing(Sizing.fixed(30), Sizing.content());
        currentStatValue.id(paramName+ statCurrentValueSuffix);
        container.child(currentStatValue);

        return container;
    }

    private void changeStatPoint(StatsOperationEnum operation, StatsEnum stat){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(operation.name());
        buf.writeString(stat.name());
        ClientPlayNetworking.send(C2SPacketHandlers.STAT_POINT_CHANGE, buf);
        //this.client.player.getWorld().getEntityById();
    }

}

/*

Problem: I need to get the progression data from the entity to display it here

My current approach: use the PetOwnerComponent to get to the PetMetadataComponent.

    - problem: i need the entity to get the PetMetadataComponent. The entity is not available in
    the client.

 */
