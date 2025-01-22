package io.github.diiiaz.fireflies.particle;

import com.mojang.serialization.MapCodec;
import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.particle.custom.FireflyParticleEffect;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.Function;

public class ModParticleTypes {

    public static final ParticleType<FireflyParticleEffect> FIREFLY = register("firefly", false, type -> FireflyParticleEffect.CODEC, type -> FireflyParticleEffect.PACKET_CODEC);

    @SuppressWarnings("SameParameterValue")
    private static <T extends ParticleEffect> ParticleType<T> register(
            String name,
            boolean alwaysShow,
            Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, PacketCodec<? super RegistryByteBuf, T>> packetCodecGetter) {
        return Registry.register(Registries.PARTICLE_TYPE, Mod.createIdentifier(name), FabricParticleTypes.complex(alwaysShow, codecGetter, packetCodecGetter));
    }


    public static void initialize() {}

}
