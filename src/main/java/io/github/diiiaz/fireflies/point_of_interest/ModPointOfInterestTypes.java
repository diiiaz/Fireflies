package io.github.diiiaz.fireflies.point_of_interest;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public class ModPointOfInterestTypes {


    public static final RegistryKey<PointOfInterestType> FIREFLY_HOME = register("firefly_home", 0, 1, ModBlocks.LUMINESCENT_SOIL);

    @SuppressWarnings("SameParameterValue")
    private static RegistryKey<PointOfInterestType> register(String name, int ticketCount, int searchDistance, Block block) {
        final Identifier id = Mod.createIdentifier(name);
        PointOfInterestHelper.register(id, ticketCount, searchDistance, block.getStateManager().getStates());
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, id);
    }

    public static void initialize() {
    }

}
