package io.github.diiiaz.fireflies.item;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.item.custom.FireflyBottle;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {

    private static final String FIREFLY_BOTTLE_NAME = "firefly_bottle";

    public static final Item FIREFLY_BOTTLE = registerItem(FIREFLY_BOTTLE_NAME,new FireflyBottle(new Item.Settings()
            .maxCount(1)
            .component(ModDataComponentTypes.FIREFLIES_AMOUNT, 1)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Mod.createIdentifier(FIREFLY_BOTTLE_NAME)))
    ));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Mod.createIdentifier(name), item);
    }


    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.addAfter(Items.CREAKING_HEART, FIREFLY_BOTTLE);
        });

    }

}
