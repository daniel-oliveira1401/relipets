package net.daniel.relipets.entity.render_layers;

import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class PartModel extends GeoModel<BasePart> {

    String partType;
    public BaseCore core;

    public PartModel(Identifier assetSubpath, String partType) {

        this.partType = partType;

        this.modelPath = buildFormattedModelPath(assetSubpath);
        this.texturePath = buildFormattedTexturePath(assetSubpath);
        this.animationsPath = buildFormattedAnimationPath(assetSubpath);

    }

    public String subtype() {
        return "parts/"+this.partType;
    }

    public Identifier modelPath;
    public Identifier texturePath;
    public Identifier animationsPath;

    public Identifier buildFormattedModelPath(Identifier basePath) {
        return new Identifier(basePath.getNamespace(), "geo/" + subtype() + "/" + basePath.getPath() + ".geo.json");
    }

    public Identifier buildFormattedAnimationPath(Identifier basePath) {
        return new Identifier(basePath.getNamespace(), "animations/" + subtype() + "/" + basePath.getPath() + ".animation.json");
    }

    public Identifier buildFormattedTexturePath(Identifier basePath) {
        return new Identifier(basePath.getNamespace(), "textures/" + subtype() + "/" + basePath.getPath() + ".png");
    }


    @Override
    public Identifier getModelResource(BasePart animatable) {
        return this.modelPath;
    }

    @Override
    public Identifier getTextureResource(BasePart animatable) {
        return this.texturePath;
    }

    public Identifier getTexture(BasePart animatable) {
        return this.texturePath;
    }

    @Override
    public Identifier getAnimationResource(BasePart animatable) {
        return this.animationsPath;
    }

    public void updateCustomAnimation(BaseCore core, float partialTick){
        this.core = core;
        this.partialTick = partialTick;
        this.setCustomAnimations(null, 0, null);
    }

    public float partialTick = 0;
    @Override
    public void setCustomAnimations(BasePart basePart, long instanceId, AnimationState<BasePart> animationState) {

        if(this.partType.equals(PetPart.HEAD_PART)){

            CoreGeoBone head = getAnimationProcessor().getBone("head");

            if (head != null && this.core != null) {

                float headPitch = MathHelper.lerp(partialTick, core.prevPitch, core.getPitch());
                //headPitch = -headPitch; //idk what is happening here but this is necessary, i think

                float lerpBodyRot = core == null ? 0 : MathHelper.lerpAngleDegrees(partialTick, core.prevBodyYaw, core.bodyYaw);
                float lerpHeadRot = core == null ? 0 : MathHelper.lerpAngleDegrees(partialTick, core.prevHeadYaw, core.headYaw);
                float netHeadYaw = lerpHeadRot - lerpBodyRot;
                netHeadYaw = -netHeadYaw; //idk what is happening here but this is necessary, i think

                head.setRotX(headPitch * MathHelper.RADIANS_PER_DEGREE);
                head.setRotY(netHeadYaw * MathHelper.RADIANS_PER_DEGREE);

            }
        }

    }

}