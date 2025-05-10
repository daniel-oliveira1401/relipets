package net.daniel.relipets.entity.render_layers;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.GeckoLibException;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;

public class BasePart implements GeoEntity {

    public boolean variantChanged;
    AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BaseCore core;
    public String partType;
    public String partModelId;

    public String activeModelId = "";

    private final HashMap<String, RawAnimation> rawAnimationCache = new HashMap<>();

    private final RawAnimation IDLE = RawAnimation.begin().thenLoop(BaseCore.ANIM_IDLE);

    public String buildAnimCacheKey(String animationName){
        return animationName+"_"+this.partModelId;
    }

    protected <E extends BasePart> PlayState partAnimController(final AnimationState<E> event) {

        if(core != null){
            //return event.setAndContinue(getPartAnimationForCoreAnimation(core.getCurrentAnim()));

            final Identifier location = new Identifier(Relipets.MOD_ID, "animations/parts/"+partType + "/" + partModelId + ".animation.json");
            final BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);

            final String animationName = core.getCurrentAnim();

            //Check if model has any animations
            if (bakedAnimations == null){
                //model does not have any animations. Don't play anything then
                return PlayState.STOP;
            }else{
                //Model has animations

                //check for variant changes
                if(!this.activeModelId.equals(partModelId)){
                    variantChanged = true;
                }

                if(variantChanged){
                    variantChanged = false;
                    this.cache = GeckoLibUtil.createInstanceCache(this);
                    this.rawAnimationCache.clear();
                    this.activeModelId = partModelId;
                    return PlayState.STOP;
                }

                //this part has an animation that corresponds to the current anim
                if(bakedAnimations.animations().containsKey(animationName)){
                    RawAnimation anim = rawAnimationCache.getOrDefault(buildAnimCacheKey(animationName), null);

                    if(anim == null){
                        rawAnimationCache.put(buildAnimCacheKey(animationName), RawAnimation.begin().thenLoop(animationName));
                    }

                    return event.setAndContinue(anim);
                }else{
                    //this part does not have an animation that corresponds exactly to the current animation.

                    //Check if the current animation is a "fly-like" animation.
                    //If it is, then check if it has a base "fly" animation.
                    if(animationName.startsWith("fly")){
                        final Animation baseFlyAnimation = bakedAnimations.getAnimation("fly");
                        //if it has, then play the base fly animation
                        if(baseFlyAnimation != null){
                            RawAnimation anim = rawAnimationCache.getOrDefault(buildAnimCacheKey("fly"), null);

                            if(anim == null){
                                rawAnimationCache.put(buildAnimCacheKey("fly"), RawAnimation.begin().thenLoop("fly"));
                            }

                            return event.setAndContinue(anim);
                        }
                    }

                    //If none of the above happens, fall back to idle. Assumes that it has an idle animation.
                    return  event.setAndContinue(IDLE);

                }

            }

        }

        //if the part doesnt have a core attached to it (like when it is being displayed as an item), then don't play anything
        return PlayState.STOP;

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<BasePart>(this, "part_anim_controller", 10, this::partAnimController));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
