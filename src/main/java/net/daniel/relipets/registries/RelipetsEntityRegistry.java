package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.entity.cores.BaseCore;
import net.daniel.relipets.entity.cores.CyanCore;
import net.daniel.relipets.items.special.PetificatorProjectile;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RelipetsEntityRegistry {

    //================= Folder paths for the defaulted core models ==========
    public static final String CYAN_CORE_ID = "cyan_core";
    public static final String CYAN_CORE_PATH = "pets/"+CYAN_CORE_ID;


    public static final EntityType<CyanCore> CYAN_CORE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Relipets.MOD_ID, CYAN_CORE_PATH),
            EntityType.Builder.create(CyanCore::new, SpawnGroup.CREATURE).setDimensions(1, 1).build(CYAN_CORE_ID)
    );

    public static final EntityType<PetificatorProjectile> PETIFICATOR_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Relipets.MOD_ID, "petificator_projectile"),
            EntityType.Builder.create(PetificatorProjectile::new, SpawnGroup.MISC).setDimensions(0.5f, 0.5f)
                    .build("petificator_projectile")
    );

    public static void onInitialize() {
        FabricDefaultAttributeRegistry.register(RelipetsEntityRegistry.CYAN_CORE, BaseCore.createMobAttributes());
    }
}
