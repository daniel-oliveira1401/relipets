package net.daniel.relipets.entity.render_layers;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.cores.BaseCore;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.DefaultedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;

public class PartRenderer extends GeoObjectRenderer<BasePart> {
    PartModel partVariantModel;
    public String partVariantId;
    public String partType;

    public boolean variantChangeSinceLastRender = true;

    public PartRenderer() {
        super(null);
    }

    public void setPartVariant(String variantId, String partType) {

        if(variantId.equals(this.partVariantId) && partType.equals(this.partType)){
            variantChangeSinceLastRender = false;
        }else{
            variantChangeSinceLastRender = true;
        }

        this.partVariantId = variantId;
        this.partType = partType;

    }

    public void buildModelIfNeeded(){

        if(!variantChangeSinceLastRender) return;

        if(!this.isValid()) return;

        this.partVariantModel = new PartModel(new Identifier(Relipets.MOD_ID, this.partVariantId), this.partType);

    }

    public boolean isValid(){
        return (this.partVariantId != null && !this.partVariantId.isEmpty() && partType != null && !partType.isEmpty());
    }


    @Override
    public GeoModel<BasePart> getGeoModel() {

        return partVariantModel;
    }

    @Override
    public Color getRenderColor(BasePart animatable, float partialTick, int packedLight) {
        if(animatable.core != null && animatable.core.hurtTime > 0){
            return Color.ofRGBA(255, 150, 150, 150);
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }
}
