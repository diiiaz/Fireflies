package io.github.diiiaz.fireflies.block;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.custom.FireflyAlcove;
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

public class ModBlocks {


    public static final Block FIREFLY_LANTERN = registerBlock("firefly_lantern",
            new FireflyLantern(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Mod.createIdentifier("firefly_lantern")))
                    .mapColor(MapColor.IRON_GRAY)
                    .solid()
                    .strength(3.5F)
                    .sounds(BlockSoundGroup.LANTERN)
                    .luminance(FireflyLantern::getLuminance)
                    .nonOpaque()
                    .pistonBehavior(PistonBehavior.DESTROY)
            ), true);

    public static final Block FIREFLY_ALCOVE = registerBlock("firefly_alcove",
            new FireflyAlcove(AbstractBlock.Settings.create()
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Mod.createIdentifier("firefly_alcove")))
                    .mapColor(MapColor.DIRT_BROWN)
                    .strength(0.5F)
                    .sounds(BlockSoundGroup.ROOTED_DIRT)
                    .pistonBehavior(PistonBehavior.BLOCK)
            ), true);




    private static Block registerBlock(String name, Block block, @SuppressWarnings("SameParameterValue") boolean createItem) {
        if (createItem) {
            registerBlockItem(name, block);
        }
        return Registry.register(Registries.BLOCK, Mod.createIdentifier(name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM,Mod.createIdentifier(name),
                new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM,Mod.createIdentifier(name)))));
    }


    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.addAfter(Items.SOUL_LANTERN, FIREFLY_LANTERN.asItem());
            entries.addAfter(Items.BEEHIVE, FIREFLY_ALCOVE.asItem());
        });

    }

}
