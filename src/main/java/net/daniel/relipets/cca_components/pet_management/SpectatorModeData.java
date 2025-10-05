package net.daniel.relipets.cca_components.pet_management;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.daniel.relipets.cca_components.ISerializable;
import net.daniel.relipets.utils.Utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;


public class SpectatorModeData implements ISerializable {
    @Getter
    @Setter
    BlockPos originalPos;

    @Getter
    @Setter
    GameMode originalGameMode;
    ServerWorld originalWorld;

    @Getter
    @Setter
    private boolean isSpectating;

    @Getter
    @Setter
    private int spectatedPetSlot;
    private Identifier originalWorldIdentifier;

    public SpectatorModeData(NbtCompound nbt){
        this.readFromNbt(nbt);
    }

    public SpectatorModeData(BlockPos originalPos, GameMode originalGameMode, ServerWorld originalWorld, boolean isSpectating, int spectatedPetSlot){
        this.originalPos = originalPos;
        this.originalGameMode = originalGameMode;
        this.setOriginalWorld(originalWorld);
        this.isSpectating = isSpectating;
        this.spectatedPetSlot = spectatedPetSlot;
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {

        if(nbt.contains("originalPos"))
            this.originalPos = Utils.deserializeBlockPos(nbt.getString("originalPos"));

        if(nbt.contains("originalWorld"))
            this.originalWorldIdentifier = Identifier.of(nbt.getString("originalWorld"));

        if(nbt.contains("originalGameModeId"))
            this.originalGameMode = GameMode.byId(nbt.getInt("originalGameModeId"));

        if(nbt.contains("slot"))
            this.spectatedPetSlot = nbt.getInt("slot");
        if(nbt.contains("isSpectating"))
            this.isSpectating = nbt.getBoolean("isSpectating");
    }

    public ServerWorld getOriginalWorld(MinecraftServer server){
        return server.getWorld(RegistryKey.of(RegistryKeys.WORLD, this.originalWorldIdentifier));
    }

    public void setOriginalWorld(ServerWorld world){
        this.originalWorld = world;
        this.originalWorldIdentifier = this.originalWorld.getRegistryKey().getValue();
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = new NbtCompound();

        if(this.originalPos != null)
            nbt.putString("originalPos", Utils.serializeBlockPos(this.originalPos));

        if(this.originalWorld != null){
            nbt.putString("originalWorld", this.originalWorldIdentifier.toString());
        }

        if(this.originalGameMode != null){
            nbt.putInt("originalGameModeId", this.originalGameMode.getId());
        }

        nbt.putInt("slot", this.spectatedPetSlot);

        nbt.putBoolean("isSpectating", this.isSpectating);

        return nbt;
    }
}
