package net.daniel.relipets.entity.render_layers;


import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.AxisTransformation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Axis;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PartRenderLayer extends GeoRenderLayer<BaseCore> {

    private final PartRenderer partRenderer;
    private final String partType;

    public PartRenderLayer(GeoRenderer<BaseCore> entityRendererIn, String partType) {
        super(entityRendererIn);
        this.partRenderer = new PartRenderer();
        this.partType = partType;
    }

    private BasePart dummyPart  = new BasePart();

    @Override
    public void render(MatrixStack poseStack, BaseCore core, BakedGeoModel bakedModel, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        PetPart part = CardinalComponentsRegistry.PART_SYSTEM_KEY.get(core).getPartByType(partType);

        dummyPart.core = core;
        dummyPart.partType = partType;
        dummyPart.partModelId = part.modelPartId;

        this.partRenderer.setPartVariant(part.modelPartId, part.partType);

        this.partRenderer.buildModelIfNeeded();

        if(!this.partRenderer.isValid()) return;

        this.partRenderer.partVariantModel.updateCustomAnimation(core, partialTick);

        poseStack.push();

        float lerpBodyRot = core == null ? 0 : MathHelper.lerpAngleDegrees(partialTick, core.prevBodyYaw, core.bodyYaw);

        poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(lerpBodyRot));

        poseStack.translate(
                part.baseCenterOffset.x,
                part.baseCenterOffset.y,
                part.baseCenterOffset.z
        );

        this.partRenderer.render(
                poseStack,
                dummyPart,
                null,
                null,
                null,
                packedLight
        );

        poseStack.pop();

    }


}