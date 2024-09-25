package net.daniel.relipets.items;

import net.daniel.relipets.items.client.wands.AirTitanWandRenderer;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AirTitanWand extends TitanWand implements GeoItem {

    //==================== Geckolib Variables =========================
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");


    public AirTitanWand(Settings settings) {
        super(settings);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private AirTitanWandRenderer wandRenderer;

            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {

                if(this.wandRenderer == null)
                    this.wandRenderer = new AirTitanWandRenderer();

                return this.wandRenderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<GeoItem> idleAnimationController = new AnimationController<>(this, "idle", 0, state -> state.setAndContinue(IDLE));
        controllers.add(idleAnimationController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }
}
