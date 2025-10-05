package net.daniel.relipets.cca_components;


import lombok.Getter;
import net.daniel.relipets.cca_components.pet_management.PetParty;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.registries.S2CPacketHandlers;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

public class PetOwnerComponent implements Component, AutoSyncedComponent, CommonTickingComponent {

    @Getter
    PetParty petParty;
    PlayerEntity player;
    public PetOwnerComponent(PlayerEntity player){
        this.player = player;
        this.petParty = new PetParty(player);
        this.petParty.setOnPartyModifiedListener(this::onPartyModified);
    }

    //TODO: override the packet assembling method for making it more lightweight to update things
    public void onPartyModified(){
        CardinalComponentsRegistry.PET_OWNER_KEY.sync(player);

    }

    @Override
    public void tick() {
        if(!player.getWorld().isClient()){
            this.getPetParty().tick((ServerWorld) player.getWorld());
        }


    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player;
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        AutoSyncedComponent.super.writeSyncPacket(buf, recipient);
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if(tag.contains(RelipetsConstantsRegistry.PET_PARTY_KEY)){
            this.petParty.readFromNbt(tag.getCompound(RelipetsConstantsRegistry.PET_PARTY_KEY));
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if(this.petParty != null){
            NbtCompound petParty = this.petParty.writeToNbt();

            tag.put(RelipetsConstantsRegistry.PET_PARTY_KEY, petParty);

        }
    }
}

/*

 */
