package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.AirTitanWand;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RelipetsItemRegistry {

    public static final Item AIR_TITAN_WAND = registerItem(new AirTitanWand(new Item.Settings()), "air_titan_wand");

    public static Item registerItem(Item item, String id){
        Identifier itemId = Identifier.of(Relipets.MOD_ID, id);

        Item registeredItem = Registry.register(Registries.ITEM, itemId, item);

        return registeredItem;
    }

    public static void initialize(){
        //nothing goes here. This is a dummy method called only so that the JVM
        //evaluates the static fields of this registry

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register((itemGroup)-> itemGroup.add(AIR_TITAN_WAND));
    }


}
