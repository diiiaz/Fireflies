package io.github.diiiaz.fireflies.world.gen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.stateprovider.PredicatedStateProvider;


public record ModLuminescentSoilFeatureConfig(PredicatedStateProvider stateProvider, BlockPredicate target) implements FeatureConfig {

    public static final Codec<ModLuminescentSoilFeatureConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            PredicatedStateProvider.CODEC.fieldOf("state_provider").forGetter(ModLuminescentSoilFeatureConfig::stateProvider),
                            BlockPredicate.BASE_CODEC.fieldOf("target").forGetter(ModLuminescentSoilFeatureConfig::target)
                    )
                    .apply(instance, ModLuminescentSoilFeatureConfig::new)
    );

}