package net.daniel.relipets.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public interface CoreLevelUp {

    Event<CoreLevelUp> EVENT = EventFactory.createArrayBacked(CoreLevelUp.class,
            (listeners) -> (pet) -> {
                for (CoreLevelUp listener : listeners) {
                    listener.onCoreLevelUp(pet);
                }
            });

    void onCoreLevelUp(LivingEntity pet);

}
