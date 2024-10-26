package net.daniel.relipets.items.client;

import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.render_layers.BasePart;
import net.daniel.relipets.entity.render_layers.PartRenderer;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PartItemRenderLayer extends GeoRenderLayer<PartItem> {
    private final PartRenderer partRenderer;

    //them: what is my purpose?
    //me: you exist to not cause a null pointer exception
    //them: oh my god
    private static final BasePart dummyPart = new BasePart();

    public PartItemRenderLayer(GeoItemRenderer<PartItem> entityRendererIn) {
        super(entityRendererIn);
        this.partRenderer = new PartRenderer();
    }

    @Override
    public void render(MatrixStack poseStack, PartItem partItem, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ItemStack stack = ((GeoItemRenderer<PartItem>) this.renderer).getCurrentItemStack();

        if(stack == null) return;

        NbtCompound partVariant = stack.getOrCreateNbt().getCompound(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY);

        PetPart part = PetPart.readFromNbt(partVariant);

        this.partRenderer.setPartVariant(part.modelPartId, part.partType);

        this.partRenderer.buildModelIfNeeded();

        if(!this.partRenderer.isValid()) return;

        poseStack.push();

        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.translate(part.baseCenterOffset.x, part.baseCenterOffset.y, part.baseCenterOffset.z);
        this.partRenderer.render(
                poseStack,
                dummyPart,
                bufferSource,
                null,
                null,
                packedLight
        );

        poseStack.pop();

    }

}
