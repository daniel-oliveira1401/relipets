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
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CyanCoreRenderer extends GeoEntityRenderer<BaseCore> {

    public PartRenderLayer torsoRenderLayer;

    public CyanCoreRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(Identifier.of(Relipets.MOD_ID, RelipetsEntityRegistry.CYAN_CORE_PATH), false));

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
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public Color getRenderColor(BaseCore animatable, float partialTick, int packedLight) {
        //TODO: make the core take the color of the torso for cooler customization
        return Color.WHITE;
    }

    @Override
    public Identifier getTexture(BaseCore animatable) {
        PetPart part = animatable.getPartFromType(PetPart.TORSO_PART);
        if(part != null && part.isValid() && part != PetPart.EMPTY_TORSO_PART){
            return PartModel.buildFormattedTexturePath(Identifier.of(Relipets.MOD_ID, part.getModelPartId()), part.getPartType());
        }
        return super.getTexture(animatable);
    }
}
