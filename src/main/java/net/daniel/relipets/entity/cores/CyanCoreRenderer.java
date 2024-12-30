package net.daniel.relipets.entity.cores;


import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PartSystemComponent;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.render_layers.PartRenderLayer;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CyanCoreRenderer extends GeoEntityRenderer<BaseCore> {


    public CyanCoreRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new Identifier(Relipets.MOD_ID, RelipetsEntityRegistry.CYAN_CORE_PATH), false));
        addRenderLayer(new PartRenderLayer(this, PetPart.ARM_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.HEAD_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.LEG_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.WING_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.TAIL_PART));
        addRenderLayer(new PartRenderLayer(this, PetPart.TORSO_PART));
    }

    @Override
    public void render(BaseCore entity, float entityYaw, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public Color getRenderColor(BaseCore animatable, float partialTick, int packedLight) {
        //TODO: make the core take the color of the torso for cooler customization
        return Color.WHITE;
    }
}

/*

Contiue here:

Switch from defaulted model to my custom model. There is a GetBakedModel method in the GeoModel class
which might lead me somewhere. I might be able to override it and go from there


 */
