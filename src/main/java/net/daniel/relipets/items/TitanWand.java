package net.daniel.relipets.items;

import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class TitanWand extends Item {

    public static final String TITAN_UUID_KEY = "ars_titans:titanUUID";
    public String titanType = "";

    public TitanWand(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        if(!user.getWorld().isClient()){


        }

        return ActionResult.FAIL;
    }
}
