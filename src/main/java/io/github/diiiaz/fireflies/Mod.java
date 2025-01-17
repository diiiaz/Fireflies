package io.github.diiiaz.fireflies;

import io.github.diiiaz.fireflies.component.ModDataComponentTypes;
import io.github.diiiaz.fireflies.entity.ModEntities;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import io.github.diiiaz.fireflies.item.ModItems;
import io.github.diiiaz.fireflies.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod implements ModInitializer {
	public static final String MOD_ID = "fireflies";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

		ModDataComponentTypes.initialize();
		ModEntities.initialize();
		ModItems.initialize();
		ModSounds.initialize();

		FabricDefaultAttributeRegistry.register(ModEntities.FIREFLY, FireflyEntity.createAttributes());

	}
}