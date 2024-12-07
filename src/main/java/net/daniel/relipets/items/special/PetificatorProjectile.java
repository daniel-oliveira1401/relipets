package net.daniel.relipets.items.special;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PetificatorProjectile extends PersistentProjectileEntity implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    public PetificatorProjectile(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    int maxAge = 300; //ticks

    @Override
    public void tick() {
        super.tick();

        if(maxAge <= 0){
            Relipets.LOGGER.debug("removed due to max age");
            this.remove(RemovalReason.DISCARDED);
        }

        maxAge--;

    }

    @Override
    protected ItemStack asItemStack() {
        return null;
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
    protected void onEntityHit(EntityHitResult entityHitResult) {

        if(!this.getWorld().isClient()){
            Relipets.LOGGER.debug("Hit entity!!");

            PlayerEntity owner = (PlayerEntity) this.getOwner();
            if(owner == null){
                Relipets.LOGGER.debug("Could not find owner of this projectile");
                return;
            }

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(owner);

            petOwnerSystem.getPetParty().addPetToParty((LivingEntity) entityHitResult.getEntity(), owner);


        }
        this.remove(RemovalReason.DISCARDED);


    }




}
