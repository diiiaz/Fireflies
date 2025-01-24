package io.github.diiiaz.fireflies.item;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.item.custom.CatchingNet;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.List;

public class ModItems {

    private static final String CATCHING_NET_KEY = "catching_net";
    private static final String FIREFLY_SPAWN_EGG_KEY = "firefly_spawn_egg";


    public static final Item CATCHING_NET = registerItem(CATCHING_NET_KEY, new CatchingNet(new Item.Settings()
            .maxCount(1)
            .component(ModDataComponentTypes.CAUGHT_ENTITIES, List.of())
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Mod.createIdentifier(CATCHING_NET_KEY)))
    ));

    public static final Item FIREFLY_SPAWN_EGG = registerItem(FIREFLY_SPAWN_EGG_KEY, new SpawnEggItem(ModEntities.FIREFLY, new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Mod.createIdentifier(FIREFLY_SPAWN_EGG_KEY)))
    ));



    @SuppressWarnings("SameParameterValue")
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Mod.createIdentifier(name), item);
    }


    public static void initialize() {
        //noinspection CodeBlock2Expr
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.addAfter(Items.LEAD, CATCHING_NET);
        });
        //noinspection CodeBlock2Expr
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.addAfter(Items.EVOKER_SPAWN_EGG, FIREFLY_SPAWN_EGG);
        });

    }

}
