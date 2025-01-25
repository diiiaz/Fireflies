package io.github.diiiaz.fireflies.world.gen.feature;

import com.mojang.serialization.Codec;
import io.github.diiiaz.fireflies.block.entity.ModBlockEntityTypes;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyData;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class ModLuminescentSoilFeature extends Feature<ModLuminescentSoilFeatureConfig> {

    public ModLuminescentSoilFeature(Codec<ModLuminescentSoilFeatureConfig> configCodec) {
        super(configCodec);
    }

    @Override
    public boolean generate(FeatureContext<ModLuminescentSoilFeatureConfig> context) {
        ModLuminescentSoilFeatureConfig contextConfig = context.getConfig();
        BlockPos blockPos = context.getOrigin();
        StructureWorldAccess structureWorldAccess = context.getWorld();
        Random random = context.getRandom();
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        return this.placeBlock(contextConfig, structureWorldAccess, random, blockPos.getY() + 1, blockPos.getY() - 2, mutable.set(blockPos));
    }

    protected boolean placeBlock(ModLuminescentSoilFeatureConfig config, StructureWorldAccess world, Random random, int topY, int bottomY, BlockPos.Mutable pos) {
        boolean bl = false;
        boolean bl2 = false;

        for (int i = topY; i > bottomY; i--) {
            pos.setY(i);
            if (config.target().test(world, pos)) {
                BlockState blockState = config.stateProvider().getBlockState(world, random, pos);
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
                world.getBlockEntity(pos, ModBlockEntityTypes.LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE).ifPresent(blockEntity -> {
                    int amountOfFireflyToSpawn = 6 + random.nextInt(10);
                    for (int j = 0; j < amountOfFireflyToSpawn; j++) {
                        blockEntity.addFirefly(FireflyData.create(random.nextInt(199), FireflyEntity.getRandomVariant(random), random.nextFloat()));
                    }
                });
                world.setBlockState(pos.offset(Direction.UP, 1), Blocks.SHORT_GRASS.getDefaultState(), Block.NOTIFY_LISTENERS);
                if (!bl2) {
                    this.markBlocksAboveForPostProcessing(world, pos);
                }

                bl = true;
                bl2 = true;
            } else {
                bl2 = false;
            }
        }

        return bl;
    }
}
