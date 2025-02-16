package net.daniel.relipets.items;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.items.client.PartItemRenderer;
import net.daniel.relipets.registries.PetPartRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PartItem extends Item implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    public String TYPE = "";

    public PartItem(Settings settings) {
        super(settings);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(
                new RenderProvider() {
                    private PartItemRenderer partItemRenderer;

                    @Override
                    public BuiltinModelItemRenderer getCustomRenderer() {

                        if(this.partItemRenderer == null)
                            this.partItemRenderer = new PartItemRenderer();

                        return this.partItemRenderer;
                    }
                }
        );
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if(!world.isClient()){
            if(!stack.hasNbt() || (!stack.getOrCreateNbt().contains(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY) && !this.TYPE.isEmpty())){
                fillNbtWithItem(stack);
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        super.onCraft(stack, world, player);
        if(!world.isClient()){
            if(!stack.hasNbt()){
                fillNbtWithItem(stack);
            }
        }

    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(stack.getOrCreateNbt().contains(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY)){
            PetPart part = PetPart.readFromNbt(stack.getOrCreateNbt().getCompound(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY));

            tooltip.add(
                    MutableText.of(new LiteralTextContent("Part variant: " + parsePartVariantName(part.getModelPartId())))
                            .setStyle(Style.EMPTY.withColor(0x5DE2E7))
                    );


        }
    }

    public String parsePartVariantName(String inputName){
        String[] words = inputName.split("_");
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        return formatted.toString().trim();
    }

    private void fillNbtWithItem(ItemStack stack){
        NbtCompound tag = stack.getOrCreateNbt();

        PetPart part = this.createPetPartFromType(TYPE);

        if(part != null){
            tag.put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, part.writeToNbt());
            Relipets.LOGGER.debug("Added NBT to item");

        }
    }

    protected PetPart createRandomPetPart(){

        List<PetPartRegistry.PetPartRegistryEntry> partModelVariants = PetPartRegistry.PART_VARIANTS;

        int randomIndex = (int) (Math.random() * partModelVariants.size());

        String chosenPart = partModelVariants.get(randomIndex).getName();

        PetPart part = PetPart.createFromString(chosenPart);

        return part;
    }

    @Nullable
    protected PetPart createPetPartFromType(String type){

        List<PetPartRegistry.PetPartRegistryEntry> partModelVariants = PetPartRegistry.PART_VARIANTS;

        partModelVariants = partModelVariants.stream().filter((p) -> PetPart.createFromString(p.getName()).partType.equals(type)).toList();

        if(partModelVariants.isEmpty())
            return null;

        int randomIndex = (int) (Math.random() * partModelVariants.size());

        String chosenPart = partModelVariants.get(randomIndex).getName();

        PetPart part = PetPart.createFromString(chosenPart);

        return part;
    }
}
