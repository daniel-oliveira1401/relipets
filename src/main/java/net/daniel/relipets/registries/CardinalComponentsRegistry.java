package net.daniel.relipets.registries;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.minecraft.util.Identifier;

public class CardinalComponentsRegistry {

    public static final ComponentKey<PetOwnerComponent> PET_OWNER_KEY = ComponentRegistry
            .getOrCreate(Identifier.of(Relipets.MOD_ID, "pet_owner"), PetOwnerComponent.class);

    public static void initilize(){
        //does nothing :)
        //here just to evaluate the component key fields
    }
}
