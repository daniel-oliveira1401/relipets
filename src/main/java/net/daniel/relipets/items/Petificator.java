package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.items.client.PetificatorRenderer;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Petificator extends Item implements GeoItem {

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public Petificator(Settings settings) {
        super(settings);
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(
                new RenderProvider() {
                    private PetificatorRenderer petificatorRenderer;

                    @Override
                    public BuiltinModelItemRenderer getCustomRenderer() {

                        if(this.petificatorRenderer == null)
                            this.petificatorRenderer = new PetificatorRenderer();

                        return this.petificatorRenderer;
                    }
                }
        );
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        System.out.println("called this");
        if(user.isSneaking()){
            PetificatorProjectile proj = new PetificatorProjectile(RelipetsEntityRegistry.PETIFICATOR_PROJECTILE, user.getWorld());
            //proj.setPos(user.getX(), user.getY()+1.3f, user.getZ());

            Vec3d pos = user.raycast(0.3f, 1, false).getPos();
            proj.setPosition(pos);
            proj.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            proj.setOwner(user);
            user.getWorld().spawnEntity(proj);
            return TypedActionResult.consume(user.getStackInHand(hand));
        }
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if(!attacker.getWorld().isClient()){
            //check if the player was holding shift
            if(attacker instanceof PlayerEntity player && player.isSneaking()){
                String targetUUID = target.getUuidAsString();
                PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
                PetData pet = petOwner.getPetParty().getPetByEntityUUID(targetUUID);

                if(pet != null){ //this entity is in the party of the player
                    petOwner.getPetParty().releasePetFromParty(pet);
                }
            }
            //check if the attacked entity was in the player's party
            //release that entity if so
        }

        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if(context.getPlayer() != null && !context.getWorld().isClient() && !context.getPlayer().isSneaking()){
            System.out.println("Called use on block");
            PlayerEntity player = context.getPlayer();
            PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            PetData pet = petOwner.getPetParty().getSelectedPet();
            if(pet != null){
                pet.teleport(context.getHitPos());
            }
            return ActionResult.CONSUME;
        }
        return super.useOnBlock(context);
    }
}
