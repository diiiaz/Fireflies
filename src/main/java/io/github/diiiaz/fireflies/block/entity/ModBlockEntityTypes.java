package io.github.diiiaz.fireflies.block.entity;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyJarBlockEntity;
import io.github.diiiaz.fireflies.block.entity.custom.LuminescentSoilBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntityTypes {


    public static final BlockEntityType<FireflyJarBlockEntity> FIREFLY_JAR_BLOCK_ENTITY_TYPE = register("firefly_jar", FabricBlockEntityTypeBuilder.create(FireflyJarBlockEntity::new, ModBlocks.FIREFLY_JAR).build());
    public static final BlockEntityType<LuminescentSoilBlockEntity> LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE = register("luminescent_soil", FabricBlockEntityTypeBuilder.create(LuminescentSoilBlockEntity::new, ModBlocks.LUMINESCENT_SOIL).build());


    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Mod.createIdentifier(path), blockEntityType);
    }

    public static void initialize() {}

}
