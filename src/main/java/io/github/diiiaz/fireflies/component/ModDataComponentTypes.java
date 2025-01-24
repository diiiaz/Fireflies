package io.github.diiiaz.fireflies.component;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyData;
import io.github.diiiaz.fireflies.item.custom.CatchingNet;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;
import java.util.function.UnaryOperator;

public class ModDataComponentTypes {


    public static final ComponentType<List<FireflyData>> FIREFLIES_AMOUNT = register(
            "fireflies", builder -> builder
                    .codec(FireflyData.LIST_CODEC)
                    .packetCodec(FireflyData.PACKET_CODEC.collect(PacketCodecs.toList()))
                    .cache()
    );

    public static final ComponentType<List<CatchingNet.CaughtEntityData>> CAUGHT_ENTITIES = register(
            "caught_entities", builder -> builder
                    .codec(CatchingNet.CaughtEntityData.LIST_CODEC)
                    .packetCodec(CatchingNet.CaughtEntityData.PACKET_CODEC.collect(PacketCodecs.toList()))
                    .cache()
    );


    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Mod.createIdentifier(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static void initialize() {}

}
