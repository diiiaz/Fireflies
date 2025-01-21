package io.github.diiiaz.fireflies.component;

import com.mojang.serialization.Codec;
import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyAlcoveBlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;
import java.util.function.UnaryOperator;

public class ModDataComponentTypes {


    public static final ComponentType<Integer> BOTTLE_FIREFLIES_AMOUNT = register("firefly_bottle_fireflies_amount", integerBuilder -> integerBuilder.codec(Codec.INT));
    public static final ComponentType<List<FireflyAlcoveBlockEntity.FireflyData>> ALCOVE_FIRELIES_AMOUNT = register(
            "alcove_fireflies_amount",
            builder -> builder.codec(FireflyAlcoveBlockEntity.FireflyData.LIST_CODEC).packetCodec(FireflyAlcoveBlockEntity.FireflyData.PACKET_CODEC.collect(PacketCodecs.toList())).cache()
    );


    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Mod.createIdentifier(name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static void initialize() {}

}
