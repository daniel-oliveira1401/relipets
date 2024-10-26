package net.daniel.relipets.entity.render_layers;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.GeckoLibException;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;

public class BasePart implements GeoEntity {

    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");

    public BaseCore core;
    public String partType;
    public String partModelId;

    private final HashMap<String, RawAnimation> rawAnimationCache = new HashMap<>();

    protected <E extends BasePart> PlayState partAnimController(final AnimationState<E> event) {

        if(core != null){
            //return event.setAndContinue(getPartAnimationForCoreAnimation(core.getCurrentAnim()));

            Identifier location = new Identifier(Relipets.MOD_ID, "animations/parts/"+partType + "/" + partModelId + ".animation.json");
            BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);

            String animationName = core.getCurrentAnim();

            //IDK how the hell this works, but it works. This can be used to check if an animation exists or not
            if (bakedAnimations == null){
                //animation doesnt exist, fallback to idle anim
                //return event.setAndContinue(IDLE);
                return PlayState.STOP;
            }else{

                RawAnimation anim = rawAnimationCache.getOrDefault(animationName, null);

                if(anim == null){
                    rawAnimationCache.put(animationName, RawAnimation.begin().thenLoop(animationName));
                }

                return event.setAndContinue(anim);
            }

        }

        return PlayState.STOP;

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<BasePart>(this, "part_anim_controller", 0, this::partAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
