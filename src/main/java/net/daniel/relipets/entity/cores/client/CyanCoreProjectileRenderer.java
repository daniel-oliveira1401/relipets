package net.daniel.relipets.entity.cores.client;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.cores.related_entities.CyanCoreProjectile;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CyanCoreProjectileRenderer extends GeoEntityRenderer<CyanCoreProjectile> {

    public CyanCoreProjectileRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(Identifier.of(Relipets.MOD_ID, "cyan_core_projectile")));
    }
}
