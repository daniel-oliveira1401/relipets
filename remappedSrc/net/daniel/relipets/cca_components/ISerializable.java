package net.daniel.relipets.cca_components;

import net.minecraft.nbt.NbtCompound;

public interface ISerializable {

    void readFromNbt(NbtCompound nbt);

    NbtCompound  writeToNbt();

}
