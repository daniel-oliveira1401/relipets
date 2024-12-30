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

    public BaseCore core;
    public String partType;
    public String partModelId;

    private final HashMap<String, RawAnimation> rawAnimationCache = new HashMap<>();

    private final RawAnimation IDLE = RawAnimation.begin().thenLoop(BaseCore.ANIM_IDLE);

    protected <E extends BasePart> PlayState partAnimController(final AnimationState<E> event) {

        if(core != null){
            //return event.setAndContinue(getPartAnimationForCoreAnimation(core.getCurrentAnim()));

            Identifier location = new Identifier(Relipets.MOD_ID, "animations/parts/"+partType + "/" + partModelId + ".animation.json");
            BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);

            String animationName = core.getCurrentAnim();

            //IDK how the hell this works, but it works. This can be used to check if an animation exists or not
            if (bakedAnimations == null){
                //model does not have any animations. Don't play anything then
                return PlayState.STOP;
            }else{
                //this part has an animation that corresponds to the current anim
                if(bakedAnimations.animations().containsKey(animationName)){
                    RawAnimation anim = rawAnimationCache.getOrDefault(animationName, null);

                    if(anim == null){
                        rawAnimationCache.put(animationName, RawAnimation.begin().thenLoop(animationName));
                    }

                    return event.setAndContinue(anim);
                }else{
                    //this part does not have an animation that corresponds to the current animation. Fall back to idle
                    //assumes that it has an idle animation
                    return  event.setAndContinue(IDLE);

                }

            }

        }

        //if the part doesnt have a core attached to it (for some reason), then don't play anything
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
