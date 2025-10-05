package net.daniel.relipets.entity.cores.related_entities;

import lombok.Setter;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.CyanCore;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CyanCoreProjectile extends PersistentProjectileEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    public CyanCoreProjectile(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }
    @Setter
    BaseCore attacker;

    int maxAge = 300; //ticks

    @Override
    public void tick() {
        super.tick();
        //ProjectileUtil.setRotationFromVelocity(this, 1);

        if(maxAge <= 0){
            Relipets.LOGGER.debug("removed due to max age");
            this.remove(RemovalReason.DISCARDED);
        }

        maxAge--;

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void onCollision(HitResult hitResult) {

        super.onCollision(hitResult);

    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {

        if(!this.getWorld().isClient() && entityHitResult.getEntity() instanceof LivingEntity entityHit){
            if(this.attacker != null){

                entityHit.damage(this.getWorld().getDamageSources().magic(), (float) attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
                entityHit.setAttacker(this.attacker);

            }
        }

        this.remove(RemovalReason.DISCARDED);

    }

    @Override
    protected ItemStack asItemStack() {
        return null;
    }


}
