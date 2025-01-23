package io.github.diiiaz.fireflies.entity.client;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.ModEntityModelLayers;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import io.github.diiiaz.fireflies.entity.custom.FireflyVariant;
import io.github.diiiaz.fireflies.utils.ModChat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;


@Environment(EnvType.CLIENT)
public class FireflyEntityRenderer extends MobEntityRenderer<FireflyEntity, FireflyEntityRenderState, FireflyEntityModel> {

    private static final Identifier TEXTURE = Mod.createIdentifier("textures/entity/firefly/firefly.png");
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
    public void updateRenderState(FireflyEntity entity, FireflyEntityRenderState state, float f) {
        super.updateRenderState(entity, state, f);
        state.baseColor = entity.getWorld().isNight() ? FireflyVariant.byId(entity.getVariant()).getColor() : -14803426;
    }

    @Override
    protected int getMixColor(FireflyEntityRenderState state) {
        return state.baseColor;
    }

    @Override
    protected int getBlockLight(FireflyEntity entity, BlockPos pos) {
        if (!entity.getWorld().isNight()) { return super.getBlockLight(entity, pos); }
        double offset = entity.getLightFrequencyOffset() * 1000;
        double frequency = MathHelper.clampedMap(entity.getLightFrequencyOffset(), 0.0, 1.0, 0.05, 0.2);
        return (int) sineWaveValue(0, 15, offset, frequency, entity.getWorld().getTime());
    }

    @SuppressWarnings("SameParameterValue")
    private static double sineWaveValue(double min, double max, double offset, double frequency, double time) {
        double amplitude = (max - min) / 2;
        double midpoint = (max + min) / 2;
        return midpoint + amplitude * Math.sin(2 * Math.PI * frequency * time + offset);
    }

}
