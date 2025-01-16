package io.github.diiiaz.fireflies.entity.client;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.ModEntityModelLayers;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;


@Environment(EnvType.CLIENT)
public class FireflyEntityRenderer extends MobEntityRenderer<FireflyEntity, FireflyEntityRenderState, FireflyEntityModel> {

    private static final Identifier TEXTURE = Identifier.of(Mod.MOD_ID, "textures/entity/firefly/firefly.png");
    private static final float SHADOW_RADIUS = 0.1f;

    public FireflyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new FireflyEntityModel(context.getPart(ModEntityModelLayers.FIREFLY)), SHADOW_RADIUS);
    }

    @Override
    public Identifier getTexture(FireflyEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public FireflyEntityRenderState createRenderState() {
        return new FireflyEntityRenderState();
    }

    @Override
    public void updateRenderState(FireflyEntity livingEntity, FireflyEntityRenderState livingEntityRenderState, float f) {
        super.updateRenderState(livingEntity, livingEntityRenderState, f);
    }

    @Override
    protected int getBlockLight(FireflyEntity entity, BlockPos pos) {
        return 15;
    }
}
