package net.daniel.relipets.mixin;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.events.PetFaintedCallback;
import net.daniel.relipets.registries.CardinalComponentsRegistry;
import net.daniel.relipets.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityDiscardMixin {

    @Inject(at = @At("HEAD"), method = "remove", cancellable = true)
    public void remove(Entity.RemovalReason reason, CallbackInfo info){

        if(reason == Entity.RemovalReason.DISCARDED){
            //check if entity is inside party
            PetMetadataComponent petMetadata = CardinalComponentsRegistry.PET_METADATA_KEY.get(this);

            if(petMetadata.getPlayerUUID() != null && !petMetadata.getPlayerUUID().isEmpty()){
                Utils.log("This entity that was discarded had an owner :)");

                //emit an event
                PetFaintedCallback.EVENT.invoker().interact((LivingEntity) (Object) this);

                info.cancel();
            }
        }

    }

}
