package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.cca_components.parts.PetPartSetType;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.PetPartRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class PartPackItem extends Item {

    public PartPackItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {

        if(!world.isClient()){

            System.out.println("Used item!!");

            //TODO: drop parts corresponding to the pack type
            PetPartSetType randomSet = PetPartSetType.values()[new Random().nextInt(PetPartSetType.values().length)];

            String suffix = randomSet.name().toLowerCase();
            List<PetPartRegistry.PetPartRegistryEntry> parts = PetPartRegistry.PART_VARIANTS.stream().filter((p)-> p.getName().endsWith(suffix)).toList();

            for(PetPartRegistry.PetPartRegistryEntry part : parts){
                 PetPart partToDrop = PetPart.createFromString(part.getName());

                ItemStack partToBeDropped = PartItemFactory.createStackByType(partToDrop.getPartType());
                partToBeDropped.getOrCreateNbt().put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, partToDrop.writeToNbt());
                ItemEntity partEntity = new ItemEntity(user.getWorld(), user.getX(), user.getY(), user.getZ(), partToBeDropped);
                partEntity.setVelocity(user.getWorld().random.nextGaussian() * 0.05, 0.2, user.getWorld().random.nextGaussian() * 0.05);
                user.getWorld().spawnEntity(partEntity);

            }


            //decrease the amount of items in the stack
            ItemStack pointItem = user.getStackInHand(hand);
            pointItem.decrement(1);

        }

        return super.use(world, user, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(MutableText.of(new LiteralTextContent("Use to get a full set of parts for a random variant!"))
                .setStyle(Style.EMPTY.withColor(0x5DE2E7)));
    }
}
