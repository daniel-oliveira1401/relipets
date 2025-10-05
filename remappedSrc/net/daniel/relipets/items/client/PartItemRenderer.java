package net.daniel.relipets.items.client;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.PartItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PartItemRenderer extends GeoItemRenderer<PartItem> {

    public PartItemRenderer() {
        super(new DefaultedItemGeoModel<>(new Identifier(Relipets.MOD_ID, "part_item")));
        addRenderLayer(new PartItemRenderLayer(this));
    }
}
