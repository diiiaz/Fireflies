package io.github.diiiaz.fireflies.block.entity;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyAlcoveBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntityTypes {


    public static final BlockEntityType<FireflyAlcoveBlockEntity> FIREFLY_ALCOVE_BLOCK_ENTITY_TYPE = register("firefly_alcove", FabricBlockEntityTypeBuilder.create(FireflyAlcoveBlockEntity::new, ModBlocks.FIREFLY_ALCOVE).build());


    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Mod.createIdentifier(path), blockEntityType);
    }

    public static void initialize() {}

}
