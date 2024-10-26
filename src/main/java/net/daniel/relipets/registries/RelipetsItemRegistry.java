package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.AirTitanWand;
import net.daniel.relipets.items.PartItem;
import net.daniel.relipets.items.Petificator;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RelipetsItemRegistry {

    public static final Item PART_ITEM = registerItem(new PartItem(new Item.Settings().maxCount(1)),"part_item");
    public static final Item PETIFICATOR_ITEM = registerItem(new Petificator(new Item.Settings().maxCount(1)),"petificator");

    public static Item registerItem(Item item, String id){
        Identifier itemId = Identifier.of(Relipets.MOD_ID, id);

        return Registry.register(Registries.ITEM, itemId, item);
    }

    public static void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(PETIFICATOR_ITEM));
    }
}
