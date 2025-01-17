package io.github.diiiaz.fireflies.component;

import com.mojang.serialization.Codec;
import io.github.diiiaz.fireflies.Mod;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ModDataComponentTypes {


    public static final ComponentType<Integer> FIREFLIES_AMOUNT = register("fireflies_amount", integerBuilder -> integerBuilder.codec(Codec.INT));



    private static <T>ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(Mod.MOD_ID, name),
                builderOperator.apply(ComponentType.builder()).build());
    }

    public static void initialize() {}

}
