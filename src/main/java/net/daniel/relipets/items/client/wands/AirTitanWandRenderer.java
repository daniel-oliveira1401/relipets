package net.daniel.relipets.items.client.wands;

import net.daniel.relipets.Relipets;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class AirTitanWandRenderer extends GeoItemRenderer<net.daniel.relipets.items.AirTitanWand> {
    public AirTitanWandRenderer() {
        super(new DefaultedItemGeoModel<>(new Identifier(Relipets.MOD_ID, "air_titan_wand")));
    }
}
