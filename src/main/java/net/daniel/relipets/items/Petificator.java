package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.items.client.PetificatorRenderer;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.registries.KeyBindingsRegistry;
import net.daniel.relipets.registries.RelipetsEntityRegistry;
import net.daniel.relipets.registries.RelipetsItemRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
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
        if(user.isSneaking()){

            //get the capsule from the player inventory
            boolean hasCapsule = user.getInventory().main.stream()
                    .anyMatch(stack -> !stack.isEmpty() && stack.isOf(RelipetsItemRegistry.CAPSULE));

            if(!hasCapsule){
                Utils.message("Capsule required for capturing pets", user);
                return TypedActionResult.fail(user.getStackInHand(hand));
            }

            for(ItemStack itemStack : user.getInventory().main){
                if(itemStack.getItem() instanceof CapsuleItem){
                    itemStack.decrement(1);
                    break;
                }
            }

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
            PlayerEntity player = context.getPlayer();
            PetOwnerComponent petOwner = CardinalComponentsRegistry.PET_OWNER_KEY.get(player);
            PetData pet = petOwner.getPetParty().getSelectedPet();
            if(pet != null){
                pet.teleport(context.getBlockPos().up().toCenterPos());
            }
            return ActionResult.CONSUME;
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(getTooltipText("[Sneak + Right Click] To throw a pet capsule"));
        tooltip.add(getTooltipText("[Sneak + Left Click] A summoned pet to release it from your pet party"));
        tooltip.add(getTooltipText("[Sneak + Scroll] While holding the wand to cycle through the pets"));

        var summonKey = KeyBindingsRegistry.toggleCurrentPetSummonStateKeyBinding.getBoundKeyLocalizedText().getString();
        tooltip.add(getTooltipText("["+summonKey + "] To summon/recall the selected pet"));

        var openConfigScreenKey = KeyBindingsRegistry.openPetConfigurationScreen.getBoundKeyLocalizedText().getString();
        tooltip.add(getTooltipText("["+openConfigScreenKey + "] To open the config screen"));
        tooltip.add(getTooltipText("[Right Click] A block with a summoned pet selected to tp it to the block"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    private MutableText getTooltipText(String text){
        return MutableText.of(new LiteralTextContent(text));
    }
}
