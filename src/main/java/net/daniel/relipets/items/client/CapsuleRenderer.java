package net.daniel.relipets.items.client;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.PartItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CapsuleRenderer extends GeoItemRenderer<PartItem> {

    public CapsuleRenderer() {
        super(new DefaultedItemGeoModel<>(Identifier.of(Relipets.MOD_ID, "capsule")));
    }


}
