package net.daniel.relipets.items;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class TorsoPartItem extends PartItem{

    public TorsoPartItem(Settings settings) {
        super(settings);
    }

    {
        TYPE = PetPart.TORSO_PART;

    }

}
