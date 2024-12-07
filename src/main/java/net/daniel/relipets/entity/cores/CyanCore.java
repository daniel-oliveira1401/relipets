package net.daniel.relipets.entity.cores;

import net.daniel.relipets.cca_components.PartSystemComponent;
import net.daniel.relipets.cca_components.parts.PetPart;
import net.daniel.relipets.items.PartItem;
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
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 10, 0.05f));
        this.goalSelector.add(2, new WanderAroundGoal(this, 0.3));
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

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if(!player.getWorld().isClient() && hand == Hand.MAIN_HAND){
            ItemStack mainHandItem = player.getMainHandStack();

            //if interact with shears, remove parts

            if(mainHandItem.getItem() instanceof ShearsItem){

                PartSystemComponent partSystem = CardinalComponentsRegistry.PART_SYSTEM_KEY.get(this);

                PetPart partRemoved = partSystem.removeNextPart();
                if(partRemoved != null){
                    ItemStack partToBeDropped = new ItemStack(RelipetsItemRegistry.PART_ITEM, 1);
                    partToBeDropped.getOrCreateNbt().put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, partRemoved.writeToNbt());
                    ItemEntity partEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), partToBeDropped);
                    partEntity.setVelocity(this.getWorld().random.nextGaussian() * 0.05, 0.2, this.getWorld().random.nextGaussian() * 0.05);
                    this.getWorld().spawnEntity(partEntity);

                }



            }else if (mainHandItem.getItem() instanceof PartItem){

                NbtCompound itemTag = mainHandItem.getOrCreateNbt().getCompound(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY);

                PetPart partInHand = PetPart.readFromNbt(itemTag);

                PartSystemComponent partSystem = CardinalComponentsRegistry.PART_SYSTEM_KEY.get(this);

                //drop the pet part if it already has one of this type
                if(partSystem.hasValidPart(partInHand.partType)){
                    PetPart existingPart = partSystem.getPartByType(partInHand.partType);

                    ItemStack partToBeDropped = new ItemStack(RelipetsItemRegistry.PART_ITEM, 1);
                    partToBeDropped.getOrCreateNbt().put(RelipetsConstantsRegistry.PART_VARIANT_ITEM_KEY, existingPart.writeToNbt());
                    ItemEntity partEntity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), mainHandItem);
                    partEntity.setVelocity(this.getWorld().random.nextGaussian() * 0.05, 0.2, this.getWorld().random.nextGaussian() * 0.05);
                    this.getWorld().spawnEntity(partEntity);
                }

                partSystem.addOrUpdatePart(partInHand);
                mainHandItem.decrement(1);
            }


        }

        return super.interactMob(player, hand);
    }
}
