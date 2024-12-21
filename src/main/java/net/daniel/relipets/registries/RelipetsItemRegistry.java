package net.daniel.relipets.registries;

import net.daniel.relipets.Relipets;
import net.daniel.relipets.items.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.EggItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RelipetsItemRegistry {

    public static final Item WING_PART_ITEM = registerItem(new WingPartItem(new Item.Settings().maxCount(1)),"wing_part_item");
    public static final Item LEG_PART_ITEM = registerItem(new LegPartItem(new Item.Settings().maxCount(1)),"leg_part_item");
    public static final Item TAIL_PART_ITEM = registerItem(new TailPartItem(new Item.Settings().maxCount(1)),"tail_part_item");
    public static final Item HEAD_PART_ITEM = registerItem(new HeadPartItem(new Item.Settings().maxCount(1)),"head_part_item");
    public static final Item TORSO_PART_ITEM = registerItem(new TorsoPartItem(new Item.Settings().maxCount(1)),"torso_part_item");
    public static final Item ARM_PART_ITEM = registerItem(new ArmPartItem(new Item.Settings().maxCount(1)),"arm_part_item");
    public static final Item PETIFICATOR_ITEM = registerItem(new Petificator(new Item.Settings().maxCount(1)),"petificator");
    public static final Item CYAN_CORE_EGG = registerItem(new SpawnEggItem(RelipetsEntityRegistry.CYAN_CORE, 0x5DE2E7, 0x1D8B90, new Item.Settings()),"cyan_core_spawn_egg");

    public static Item registerItem(Item item, String id){
        Identifier itemId = Identifier.of(Relipets.MOD_ID, id);

        return Registry.register(Registries.ITEM, itemId, item);
    }

    public static void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(WING_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(LEG_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(TAIL_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(HEAD_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(TORSO_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(ARM_PART_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register((itemGroup) -> itemGroup.add(PETIFICATOR_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((itemGroup) -> itemGroup.add(CYAN_CORE_EGG));
    }
}
