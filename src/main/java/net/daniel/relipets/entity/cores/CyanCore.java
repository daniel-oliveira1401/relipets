package net.daniel.relipets.entity.cores;

import net.daniel.relipets.cca_components.PartSystemComponent;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.items.PartItemFactory;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsConstantsRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CyanCore extends BaseCore {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    //the core should have a way to access its parts

    public CyanCore(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    int tickCount = 0;
    @Override
    protected void mobTick() {
        super.mobTick();
        if(!this.getWorld().isClient()){

            if(tickCount >= 100){
                if(this.getCurrentAnim().isEmpty()){
                    this.setCurrentAnim("idle");
                }
                tickCount = 0;
            }

            tickCount++;

        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height / 2;
    }

}
