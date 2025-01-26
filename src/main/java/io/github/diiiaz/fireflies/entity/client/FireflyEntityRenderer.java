package io.github.diiiaz.fireflies.entity.client;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.ModEntityModelLayers;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.TintableCompositeModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;


@Environment(EnvType.CLIENT)
public class FireflyEntityRenderer extends MobEntityRenderer<FireflyEntity, TintableCompositeModel<FireflyEntity>> {

    private static final Identifier TEXTURE = Mod.createIdentifier("textures/entity/firefly/firefly.png");
    private static final float SHADOW_RADIUS = 0.1f;

    public FireflyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new FireflyEntityModel<>(context.getPart(ModEntityModelLayers.FIREFLY)), SHADOW_RADIUS);
    }

    @Override
    public Identifier getTexture(FireflyEntity entity) {
        return TEXTURE;
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
