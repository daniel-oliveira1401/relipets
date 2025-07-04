package net.daniel.relipets.gui.screen;

import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.cca_components.pet_management.PetInventoryManager;
import net.daniel.relipets.cca_components.pet_management.event.PetPartyUpdateNotifier;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.util.PetCameraEntity;
import net.daniel.relipets.registries.C2SPacketHandlers;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PetSpectatorScreen extends BaseOwoScreen<FlowLayout> {

    FlowLayout rootComponent;
    private final PetData petData;
    private PetPartyUpdateNotifier.Subscriber sub;
    private final int slot;
    private PetCameraEntity cameraEntity;
    private FlowLayout backgroundPlaceholder;

    public PetSpectatorScreen(int selectedSlot, PetData petData){
        this.slot = selectedSlot;
        this.petData = petData;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        this.sub = (p)-> {
            this.rootComponent.queue(()-> {

                if(this.client == null || this.client.world == null || this.client.player == null) return;

                this.client.world.getEntitiesByClass(LivingEntity.class, this.client.player.getBoundingBox().expand(60), (e)-> {
                    return e.getUuidAsString().equals(this.petData.getPetEntityData().getEntityUUID());
                }).stream().findFirst().ifPresent((entity)-> {
                    this.cameraEntity = new PetCameraEntity(this.client.world, entity);

                    cameraEntity.setPosition(
                            entity.getX() + 5,
                            entity.getY() + 2,
                            entity.getZ()
                    );
                    cameraEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, entity.getPos());
                    this.client.world.addEntity(-65, cameraEntity);

                    this.client.setCameraEntity(cameraEntity);
                    this.client.options.setPerspective(Perspective.THIRD_PERSON_BACK);
                    this.rootComponent.surface(Surface.flat(0x00000000));
                    this.rootComponent.removeChild(this.backgroundPlaceholder);
                });


            });
        };
        PetPartyUpdateNotifier.getInstance().subscribe(sub);
        this.rootComponent = rootComponent;
        rootComponent.surface(Surface.flat(0xff000000));
        rootComponent.padding(Insets.both(15, 15));
        rootComponent.child(
                Components.button(Text.of("Exit Spectator"), (b)-> exitSpectator())
        );
        this.backgroundPlaceholder = Containers.verticalFlow(Sizing.fill(100), Sizing.fill(80)).child(
                Components.label(Text.of("Connecting to Live Pet Camera...")).color(Color.WHITE)
        );

        this.backgroundPlaceholder.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        rootComponent.child(
            this.backgroundPlaceholder
        );

    }

    private void exitSpectator() {
        if(this.client != null){
            this.close();
        }
    }

    private void unloadAreaAroundPet(){

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(this.slot);

        ClientPlayNetworking.send(C2SPacketHandlers.UNLOAD_AREA_AROUND_PET, buf);
    }

    @Override
    public void close() {
        PetPartyUpdateNotifier.getInstance().unsubscribe(sub);
        if(this.client != null){
            this.client.setCameraEntity(this.client.player);
            this.client.options.setPerspective(Perspective.FIRST_PERSON);

            if(cameraEntity != null)
                cameraEntity.remove(Entity.RemovalReason.DISCARDED);

        }
        unloadAreaAroundPet();

        super.close();
    }
}

/*

Problem: I need to get the progression data from the entity to display it here

My current approach: use the PetOwnerComponent to get to the PetMetadataComponent.

    - problem: i need the entity to get the PetMetadataComponent. The entity is not available in
    the client.

 */
