package io.github.diiiaz.fireflies;

import io.github.diiiaz.fireflies.entity.ModEntityModelLayers;
import io.github.diiiaz.fireflies.entity.ModEntityRenderer;
import net.fabricmc.api.ClientModInitializer;

public class ModClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ModEntityRenderer.initialize();
        ModEntityModelLayers.initialize();
    }

}
