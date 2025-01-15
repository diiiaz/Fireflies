package io.github.diiiaz.fireflies.entity;


import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.client.FireflyEntityModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModEntityModelLayers {


    public static final Identifier id = Identifier.of(Mod.MOD_ID, "firefly");
    public static final EntityModelLayer FIREFLY = create(id, FireflyEntityModel::getTexturedModelData);


    private static EntityModelLayer create(Identifier id, EntityModelLayerRegistry.TexturedModelDataProvider modelDataProvider) {
        EntityModelLayer model = new EntityModelLayer(id, "main");
        EntityModelLayerRegistry.registerModelLayer(model, modelDataProvider);
        return model;
    }

    public static void register() {}

}
