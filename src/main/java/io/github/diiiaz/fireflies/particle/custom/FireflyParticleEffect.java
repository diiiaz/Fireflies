package io.github.diiiaz.fireflies.particle.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.diiiaz.fireflies.particle.ModParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.AbstractDustParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;

public class FireflyParticleEffect extends AbstractDustParticleEffect {

    public static final MapCodec<FireflyParticleEffect> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codecs.RGB.fieldOf("color").forGetter(particle -> particle.color), SCALE_CODEC.fieldOf("scale").forGetter(AbstractDustParticleEffect::getScale)
                    )
                    .apply(instance, FireflyParticleEffect::new)
    );
    public static final PacketCodec<RegistryByteBuf, FireflyParticleEffect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, particle -> particle.color, PacketCodecs.FLOAT, AbstractDustParticleEffect::getScale, FireflyParticleEffect::new
    );
    private final int color;

    public FireflyParticleEffect(int color, float scale) {
        super(scale);
        this.color = color;
    }

    @Override
    public ParticleType<FireflyParticleEffect> getType() {
        return ModParticleTypes.FIREFLY;
    }

    public Vector3f getColor() {
        return ColorHelper.toVector(this.color);
    }


    public static FireflyParticleEffect createDefault(Random random) {
        return new FireflyParticleEffect(ColorHelper.fromFloats(1.0F,
                MathHelper.map(random.nextFloat(), 0.0F, 1.0F, 0.8F, 1.0F), // red
                MathHelper.map(random.nextFloat(), 0.0F, 1.0F, 0.8F, 1.0F), // green
                0.0F), 1.0F);
    }

}
