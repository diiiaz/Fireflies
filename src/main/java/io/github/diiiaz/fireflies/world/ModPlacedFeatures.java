package io.github.diiiaz.fireflies.world;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.*;

import java.util.List;

public class ModPlacedFeatures {



    public static final RegistryKey<PlacedFeature> LUMINESCENT_SOIL_PATCH_KEY = registerKey("luminescent_soil_patch");




    public static void bootstrap(Registerable<PlacedFeature> context) {
        var configuredFeatures = context.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);

        register(
                context,
                LUMINESCENT_SOIL_PATCH_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.LUMINESCENT_SOIL_PATCH_KEY),
                CountPlacementModifier.of(UniformIntProvider.create(1, 3)),
                SquarePlacementModifier.of(),
                RandomOffsetPlacementModifier.vertically(ConstantIntProvider.create(-1)),
                PlacedFeatures.WORLD_SURFACE_WG_HEIGHTMAP,
                BiomePlacementModifier.of(),
                BlockFilterPlacementModifier.of(
                        BlockPredicate.allOf(
                                BlockPredicate.IS_AIR,
                                BlockPredicate.anyOf(

//                                      CP: Origin position of search    Z
//                                      00 01 02 03 04                   ^
//                                      05 06 07 08 09                   |
//                                      10 11 CP 12 13                   |
//                                      14 15 16 17 18                   |
//                                      19 20 21 22 23     X <-----------+

                                        BlockPredicate.matchingFluids(new BlockPos(2, -1, 2), Fluids.WATER, Fluids.FLOWING_WATER), // 00
                                        BlockPredicate.matchingFluids(new BlockPos(1, -1, 2), Fluids.WATER, Fluids.FLOWING_WATER), // 01
                                        BlockPredicate.matchingFluids(new BlockPos(0, -1, 2), Fluids.WATER, Fluids.FLOWING_WATER), // 02
                                        BlockPredicate.matchingFluids(new BlockPos(-1, -1, 2), Fluids.WATER, Fluids.FLOWING_WATER), // 03
                                        BlockPredicate.matchingFluids(new BlockPos(-2, -1, 2), Fluids.WATER, Fluids.FLOWING_WATER), // 04

                                        BlockPredicate.matchingFluids(new BlockPos(2, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), // 05
                                        BlockPredicate.matchingFluids(new BlockPos(1, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), // 06
                                        BlockPredicate.matchingFluids(new BlockPos(0, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), // 07
                                        BlockPredicate.matchingFluids(new BlockPos(-1, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), // 08
                                        BlockPredicate.matchingFluids(new BlockPos(-2, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), // 09

                                        BlockPredicate.matchingFluids(new BlockPos(2, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), // 10
                                        BlockPredicate.matchingFluids(new BlockPos(1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), // 11
                                        BlockPredicate.matchingFluids(new BlockPos(-1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), // 12
                                        BlockPredicate.matchingFluids(new BlockPos(-2, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), // 13

                                        BlockPredicate.matchingFluids(new BlockPos(2, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER), // 14
                                        BlockPredicate.matchingFluids(new BlockPos(1, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER), // 15
                                        BlockPredicate.matchingFluids(new BlockPos(0, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER), // 16
                                        BlockPredicate.matchingFluids(new BlockPos(-1, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER), // 17
                                        BlockPredicate.matchingFluids(new BlockPos(-2, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER), // 18

                                        BlockPredicate.matchingFluids(new BlockPos(2, -1, -2), Fluids.WATER, Fluids.FLOWING_WATER), // 19
                                        BlockPredicate.matchingFluids(new BlockPos(1, -1, -2), Fluids.WATER, Fluids.FLOWING_WATER), // 20
                                        BlockPredicate.matchingFluids(new BlockPos(0, -1, -2), Fluids.WATER, Fluids.FLOWING_WATER), // 21
                                        BlockPredicate.matchingFluids(new BlockPos(-1, -1, -2), Fluids.WATER, Fluids.FLOWING_WATER), // 22
                                        BlockPredicate.matchingFluids(new BlockPos(-2, -1, -2), Fluids.WATER, Fluids.FLOWING_WATER) // 23

                                )
                        )
                ));

    }


    public static RegistryKey<PlacedFeature> registerKey(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, Mod.createIdentifier(name));
    }

    private static void register(Registerable<PlacedFeature> context, RegistryKey<PlacedFeature> key, RegistryEntry<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    @SuppressWarnings("unused")
    private static <FC extends FeatureConfig, F extends Feature<FC>> void register(Registerable<PlacedFeature> context, @SuppressWarnings("SameParameterValue") RegistryKey<PlacedFeature> key,
                                                                                   RegistryEntry<ConfiguredFeature<?, ?>> configuration,
                                                                                   PlacementModifier... modifiers) {
        register(context, key, configuration, List.of(modifiers));
    }

}
