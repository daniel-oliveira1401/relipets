package net.daniel.relipets.items;

import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.daniel.relipets.cca_components.pet_management.PetData;
import net.daniel.relipets.items.client.PetificatorRenderer;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {

        if(!entity.getWorld().isClient() && user.isSneaking()){

            PetOwnerComponent petOwnerSystem = CardinalComponentsRegistry.PET_OWNER_KEY.get(user);

            petOwnerSystem.getPetParty().addPetToParty(entity, user);

        }

        return super.useOnEntity(stack, user, entity, hand);
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


}
