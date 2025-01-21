package io.github.diiiaz.fireflies.datagen;

import io.github.diiiaz.fireflies.point_of_interest.ModPointOfInterestTypes;
import io.github.diiiaz.fireflies.utils.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.concurrent.CompletableFuture;

public class ModPointOfInterestTypeTagProvider extends FabricTagProvider<PointOfInterestType> {


    public ModPointOfInterestTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.POINT_OF_INTEREST_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(ModTags.PointOfInterestTypes.FIREFLY_HOME).add(ModPointOfInterestTypes.FIREFLY_HOME);
    }
}
