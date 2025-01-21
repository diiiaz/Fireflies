package io.github.diiiaz.fireflies.datagen;

import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {


    public ModLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.FIREFLY_ALCOVE, this::fireflyAlcoveDrops);
        addDrop(ModBlocks.FIREFLY_LANTERN);
    }


    public LootTable.Builder fireflyAlcoveDrops(Block drop) {
        return LootTable.builder()
                .pool(
                        LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1.0F))
                                .with(
                                        ItemEntry.builder(drop)
                                                .conditionally(this.createSilkTouchCondition())
                                                .apply(CopyComponentsLootFunction.builder(CopyComponentsLootFunction.Source.BLOCK_ENTITY).include(ModDataComponentTypes.ALCOVE_FIRELIES_AMOUNT))
                                                .alternatively(ItemEntry.builder(drop))
                                )
                );
    }

}
