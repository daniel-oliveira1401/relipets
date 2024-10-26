package net.daniel.relipets.items.client;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.PartItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PetificatorRenderer extends GeoItemRenderer<PartItem> {

    public PetificatorRenderer() {
        super(new DefaultedItemGeoModel<>(new Identifier(Relipets.MOD_ID, "petificator")));
    }
}
