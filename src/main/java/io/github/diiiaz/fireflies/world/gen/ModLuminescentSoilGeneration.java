package io.github.diiiaz.fireflies.world.gen;

import io.github.diiiaz.fireflies.world.ModPlacedFeatures;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.gen.GenerationStep;

public class ModLuminescentSoilGeneration {

    public static void generateSoil() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.HAS_CLOSER_WATER_FOG), GenerationStep.Feature.LOCAL_MODIFICATIONS, ModPlacedFeatures.LUMINESCENT_SOIL_PATCH_KEY);
    }

}
