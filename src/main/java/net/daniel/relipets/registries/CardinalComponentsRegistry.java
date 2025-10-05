package net.daniel.relipets.registries;



import net.daniel.relipets.Relipets;
import net.daniel.relipets.cca_components.PetMetadataComponent;
import net.daniel.relipets.cca_components.PetOwnerComponent;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

public class CardinalComponentsRegistry {

    public static final ComponentKey<PetOwnerComponent> PET_OWNER_KEY = ComponentRegistry
            .getOrCreate(new Identifier(Relipets.MOD_ID, "pet_owner"), PetOwnerComponent.class);

    public static final ComponentKey<PetMetadataComponent> PET_METADATA_KEY = ComponentRegistry
            .getOrCreate(new Identifier(Relipets.MOD_ID, "pet_metadata"), PetMetadataComponent.class);


    public static void onInitialize() {

    }
}