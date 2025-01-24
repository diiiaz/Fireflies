package io.github.diiiaz.fireflies.block.entity;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModBlocks;
import io.github.diiiaz.fireflies.block.entity.custom.FireflyLanternBlockEntity;
import io.github.diiiaz.fireflies.block.entity.custom.LuminescentSoilBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntityTypes {


    public static final BlockEntityType<LuminescentSoilBlockEntity> LUMINESCENT_SOIL_BLOCK_ENTITY_TYPE = register("luminescent_soil", FabricBlockEntityTypeBuilder.create(LuminescentSoilBlockEntity::new, ModBlocks.LUMINESCENT_SOIL).build());
    public static final BlockEntityType<FireflyLanternBlockEntity> FIREFLY_LANTERN_BLOCK_ENTITY_TYPE = register("firefly_lantern", FabricBlockEntityTypeBuilder.create(FireflyLanternBlockEntity::new, ModBlocks.FIREFLY_LANTERN).build());


    public static <T extends BlockEntityType<?>> T register(String path, T blockEntityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, Mod.createIdentifier(path), blockEntityType);
    }

    public static void initialize() {}

}
