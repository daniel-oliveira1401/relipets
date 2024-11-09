package net.daniel.relipets.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

public interface PetFaintedCallback {

    Event<PetFaintedCallback> EVENT = EventFactory.createArrayBacked(PetFaintedCallback.class,
            (listeners) -> (pet) -> {
                for (PetFaintedCallback listener : listeners) {
                    listener.interact(pet);
                }
            });

    void interact(LivingEntity pet);
}
