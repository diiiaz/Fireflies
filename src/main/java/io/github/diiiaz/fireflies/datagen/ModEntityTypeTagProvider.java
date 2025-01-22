package io.github.diiiaz.fireflies.datagen;

import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.utils.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EntityTypeTags;

import java.util.concurrent.CompletableFuture;

public class ModEntityTypeTagProvider extends FabricTagProvider<EntityType<?>> {


    public ModEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENTITY_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.getOrCreateTagBuilder(ModTags.EntityTypes.LUMINESCENT_SOIL_INHABITORS).add(ModEntities.FIREFLY);
        this.getOrCreateTagBuilder(EntityTypeTags.FROG_FOOD).add(ModEntities.FIREFLY);
    }
}
