package net.daniel.relipets.entity.cores;


import net.daniel.relipets.entity.cores.related_entities.CyanCoreProjectile;

import net.daniel.relipets.registries.RelipetsEntityRegistry;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CyanCore extends BaseCore {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //the core should have a way to access its parts

    public CyanCore(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<CyanCore>(this, this::movementAnimController));
    }

    private PlayState movementAnimController(software.bernie.geckolib.core.animation.AnimationState<CyanCore> animationState) {
        if (animationState.isMoving() && this.isOnGround()) {
            this.setCurrentAnim(BaseCore.ANIM_WALK);

        } else if (!this.isOnGround()) {
            //looking up -> flapping
            this.setCurrentAnim(BaseCore.ANIM_FLY);
        } else {
            this.setCurrentAnim(BaseCore.ANIM_IDLE);
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2;
    }

    @Override
    public void performBasicAttack(LivingEntity attackTarget) {
        Vec3d direction = attackTarget.getPos().subtract(this.getPos()).normalize();

        CyanCoreProjectile proj = new CyanCoreProjectile(RelipetsEntityRegistry.CYAN_CORE_PROJECTILE, this.getWorld());
        proj.setAttacker(this);

        this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, attackTarget.getPos());
        proj.setPosition(this.getX(), this.getEyeY() - 0.1, this.getZ());
        proj.setVelocity(direction.multiply(0.4f));
        proj.setNoGravity(true);
        this.getWorld().spawnEntity(proj);
    }

    @Override
    public int getAttackingRange() {
        return 10;
    }
}
