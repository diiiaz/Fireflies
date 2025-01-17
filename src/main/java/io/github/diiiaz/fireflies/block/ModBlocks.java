package io.github.diiiaz.fireflies.block;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.custom.FireflyLantern;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {


    public static final Block FIREFLY_LANTERN = registerBlock("firefly_lantern",
            new FireflyLantern(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Mod.MOD_ID, "firefly_lantern")))
                    .mapColor(MapColor.IRON_GRAY)
                    .solid()
                    .strength(3.5F)
                    .sounds(BlockSoundGroup.LANTERN)
                    .luminance(FireflyLantern::getLuminance)
                    .nonOpaque()
                    .pistonBehavior(PistonBehavior.DESTROY)
            ));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Mod.MOD_ID, name), block);
    }


    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(Mod.MOD_ID, name),
                new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Mod.MOD_ID, name)))));
    }

    public static void initialize() {

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.SOUL_LANTERN, FIREFLY_LANTERN.asItem());
        });

    }

}
