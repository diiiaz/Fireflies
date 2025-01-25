package io.github.diiiaz.fireflies.world;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.world.gen.feature.ModFeatures;
import io.github.diiiaz.fireflies.world.gen.feature.ModLuminescentSoilFeatureConfig;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;

import java.util.List;

public class ModConfiguredFeatures {



    public static final RegistryKey<ConfiguredFeature<?, ?>> LUMINESCENT_SOIL_PATCH_KEY = registerKey("luminescent_soil_patch");



    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> context) {
        register(context, LUMINESCENT_SOIL_PATCH_KEY,
                ModFeatures.LUMINESCENT_SOIL_FEATURE,
                new ModLuminescentSoilFeatureConfig(
                        PredicatedStateProvider.of(ModBlocks.LUMINESCENT_SOIL),
                        BlockPredicate.matchingBlocks(List.of(Blocks.DIRT, Blocks.GRASS_BLOCK))
                )
        );
    }

    public static RegistryKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_FEATURE, Mod.createIdentifier(name));
    }

    @SuppressWarnings("SameParameterValue")
    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(Registerable<ConfiguredFeature<?, ?>> context, RegistryKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

}
