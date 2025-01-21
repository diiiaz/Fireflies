package io.github.diiiaz.fireflies.datagen;

import io.github.diiiaz.fireflies.utils.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModEnchantmentProvider extends FabricTagProvider.EnchantmentTagProvider {


    public ModEnchantmentProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(ModTags.Enchantments.PREVENTS_FIREFLY_SPAWNS_WHEN_MINING).add(Enchantments.SILK_TOUCH);
    }
}
