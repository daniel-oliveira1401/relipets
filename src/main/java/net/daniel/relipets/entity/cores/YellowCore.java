package net.daniel.relipets.entity.cores;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class YellowCore extends BaseCore{

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public YellowCore(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<YellowCore>(this, this::movementAnimController));
    }

    private PlayState movementAnimController(software.bernie.geckolib.core.animation.AnimationState<YellowCore> animationState) {
        if(animationState.isMoving()){
            this.setCurrentAnim(BaseCore.ANIM_WALK);

        }else{
            this.setCurrentAnim(BaseCore.ANIM_IDLE);
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
