package io.github.diiiaz.fireflies;

import io.github.diiiaz.fireflies.datagen.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;


@SuppressWarnings("unused")
public class ModDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModEntityTypeTagProvider::new);
		pack.addProvider(ModPointOfInterestTypeTagProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModLootTableProvider::new);
		pack.addProvider(ModEnchantmentProvider::new);
		pack.addProvider(ModRecipeProvider::new);

	}
}
