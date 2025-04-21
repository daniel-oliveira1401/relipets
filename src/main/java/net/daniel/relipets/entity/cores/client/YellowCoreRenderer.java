package net.daniel.relipets.entity.cores.client;


import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.render_layers.PartModel;
import net.daniel.relipets.entity.render_layers.PartRenderLayer;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class YellowCoreRenderer extends GeoEntityRenderer<BaseCore> {

    public PartRenderLayer torsoRenderLayer;

    public YellowCoreRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new Identifier(Relipets.MOD_ID, RelipetsEntityRegistry.YELLOW_CORE_PATH), false));

        addRenderLayer(new PartRenderLayer(this, PetPart.ARM_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.HEAD_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.LEG_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.WING_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.TAIL_PART));
        this.torsoRenderLayer = new PartRenderLayer(this, PetPart.TORSO_PART);
        addRenderLayer(this.torsoRenderLayer);
    }

    @Override
    public void render(BaseCore entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        //poseStack.push();

        //float lerpPitch = entity == null ? 0 : MathHelper.lerp(partialTick, entity.prevPitch, entity.getPitch());
        //float lerpYaw = entity == null ? 0 : MathHelper.lerp(partialTick, entity.prevYaw, entity.getYaw());

        // First rotate yaw around the Y-axis
        //poseStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(entityYaw/2));

        // Then rotate pitch around the X-axis (now local to the yaw)
        //poseStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-lerpPitch));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        //poseStack.pop();
    }

    @Override
    public Identifier getTexture(BaseCore animatable) {
        PetPart part = animatable.getPartFromType(PetPart.TORSO_PART);
        if(part != null && part.isValid() && part != PetPart.EMPTY_TORSO_PART){
            return PartModel.buildFormattedTexturePath(new Identifier(Relipets.MOD_ID, part.getModelPartId()), part.getPartType());
        }
        return super.getTexture(animatable);
    }
}

/*

Contiue here:

Switch from defaulted model to my custom model. There is a GetBakedModel method in the GeoModel class
which might lead me somewhere. I might be able to override it and go from there


 */
