package net.daniel.relipets.cca_components;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;

public class PetMetadataComponent implements Component, AutoSyncedComponent {

    public static final String PLAYER_UUID_KEY = "player_uuid";
    LivingEntity petEntity;
    @Getter
    @Setter
    String playerUUID = "";

    public PetMetadataComponent(LivingEntity pet){
        this.petEntity = pet;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        if(tag.contains(PLAYER_UUID_KEY))
            this.playerUUID = tag.getString(PLAYER_UUID_KEY);
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putString(PLAYER_UUID_KEY, this.playerUUID);
    }

    public void clear(){
        this.playerUUID = "";
    }
}
