package net.daniel.relipets.registries;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PartSystemComponent;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.minecraft.util.Identifier;

public class CardinalComponentsRegistry {

    public static final ComponentKey<PetOwnerComponent> PET_OWNER_KEY = ComponentRegistry
            .getOrCreate(new Identifier(Relipets.MOD_ID, "pet_owner"), PetOwnerComponent.class);

    public static final ComponentKey<PartSystemComponent> PART_SYSTEM_KEY = ComponentRegistry
            .getOrCreate(new Identifier(Relipets.MOD_ID, "part_system"), PartSystemComponent.class);

    public static final ComponentKey<PetMetadataComponent> PET_METADATA_KEY = ComponentRegistry
            .getOrCreate(new Identifier(Relipets.MOD_ID, "pet_metadata"), PetMetadataComponent.class);


    public static void onInitialize() {

    }
}