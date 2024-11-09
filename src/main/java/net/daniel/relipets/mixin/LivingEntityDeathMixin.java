package net.daniel.relipets.mixin;

import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.events.PetFaintedCallback;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityDeathMixin {

    @Inject(at = @At("HEAD"), method = "onDeath", cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo info){

        //check if entity is inside party
        PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(this);

        if(petMetadata.getPlayerUUID() != null && !petMetadata.getPlayerUUID().isEmpty()){
            System.out.println("This entity that died had an owner :)");

            //emit an event
            PetFaintedCallback.EVENT.invoker().interact((LivingEntity) (Object) this);

            info.cancel();
        }

        //if it is, cancel death and let the pet owner system handle what should happen next

    }

    @Inject(at = @At("HEAD"), method = "canUsePortals", cancellable = true)
    public void canUsePortals(CallbackInfoReturnable<Boolean> cir){

        //check if entity is inside party
        PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(this);

        if(petMetadata.getPlayerUUID() != null && !petMetadata.getPlayerUUID().isEmpty()){
            cir.setReturnValue(false);
        }

        //if it is, cancel death and let the pet owner system handle what should happen next

    }


}
