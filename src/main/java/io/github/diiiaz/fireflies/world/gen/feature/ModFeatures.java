package io.github.diiiaz.fireflies.world.gen.feature;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;

public class ModFeatures {

    public static final Feature<ModLuminescentSoilFeatureConfig> LUMINESCENT_SOIL_FEATURE = register("luminescent_soil_feature", new ModLuminescentSoilFeature(ModLuminescentSoilFeatureConfig.CODEC));


    private static <C extends FeatureConfig, F extends Feature<C>> F register(@SuppressWarnings("SameParameterValue") String name, F feature) {
        return Registry.register(Registries.FEATURE, Mod.createIdentifier(name), feature);
    }

    public static void initialize() {}

}
