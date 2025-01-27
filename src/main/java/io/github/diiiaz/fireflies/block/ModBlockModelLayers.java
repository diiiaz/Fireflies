package io.github.diiiaz.fireflies.block;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class ModBlockModelLayers {





    public static void initialize() {

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.FIREFLY_JAR, RenderLayer.getCutout());

    }


}
