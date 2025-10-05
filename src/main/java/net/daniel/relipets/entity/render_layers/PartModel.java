package net.daniel.relipets.entity.render_layers;

import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

import java.util.Arrays;

public class PartModel extends GeoModel<BasePart> {

    String partType;
    public BaseCore core;

    public PartModel(Identifier partVariant, String partType) {

        this.partType = partType;

        this.modelPath = buildFormattedModelPath(partVariant, partType);
        this.texturePath = buildFormattedTexturePath(partVariant, partType);
        this.animationsPath = buildFormattedAnimationPath(partVariant, partType);

    }

    public Identifier modelPath;
    public Identifier texturePath;
    public Identifier animationsPath;

    public Identifier buildFormattedModelPath(Identifier partVariant, String partType) {
        return Identifier.of(partVariant.getNamespace(), "geo/parts/" + partType + "/" + partVariant.getPath() + ".geo.json");
    }

    public static Identifier buildFormattedAnimationPath(Identifier partVariant, String partType) {
        return Identifier.of(partVariant.getNamespace(), "animations/parts/" + partType + "/" + partVariant.getPath() + ".animation.json");
    }

    public static Identifier buildFormattedTexturePath(Identifier partVariant, String partType) {
        //if(partType.equals(PetPart.TORSO_PART)){
            //return Identifier.of(basePath.getNamespace(), "textures/parts/torso/" + basePath.getPath() + ".png");
        //}else{
            return Identifier.of(partVariant.getNamespace(), "textures/parts/sets/" + getPartSet(partVariant) + ".png");
        //}
    }

    private static String getPartSet(Identifier partVariant){
        //meant to remove the partType of the variant like arm_basic_bee becomes basic_bee
        return Arrays.stream(partVariant.getPath().split("_")).skip(1).reduce("", (a, b)-> a + "_" + b).substring(1);
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
