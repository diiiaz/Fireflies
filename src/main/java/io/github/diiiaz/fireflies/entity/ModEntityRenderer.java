package io.github.diiiaz.fireflies.entity;

import io.github.diiiaz.fireflies.entity.client.FireflyEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class ModEntityRenderer {


    public static void register() {
        EntityRendererRegistry.register(ModEntities.FIREFLY, FireflyEntityRenderer::new);
    }
}
