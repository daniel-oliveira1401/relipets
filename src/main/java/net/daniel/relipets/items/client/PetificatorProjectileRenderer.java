package net.daniel.relipets.items.client;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class PetificatorProjectileRenderer extends GeoEntityRenderer<PetificatorProjectile> {

    public PetificatorProjectileRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(Identifier.of(Relipets.MOD_ID, "petificator_projectile")));
    }
}
