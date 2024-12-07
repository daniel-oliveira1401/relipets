package net.daniel.relipets.items;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.items.client.PartItemRenderer;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PartItem extends Item implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

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
            if(!stack.hasNbt()){
                NbtCompound tag = stack.getOrCreateNbt();

                PetPart part = createRandomPetPart();

                tag.put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, part.writeToNbt());
                Relipets.LOGGER.debug("Added NBT to item");
            }
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    private PetPart createRandomPetPart(){

        String[] partModelVariants = Relipets.CONFIG.partModelVariants();

        int randomIndex = (int) (Math.random() * partModelVariants.length);

        String chosenPart = partModelVariants[randomIndex];

        PetPart part = PetPart.createFromString(chosenPart);

        return part;
    }
}
