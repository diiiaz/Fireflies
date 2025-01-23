package io.github.diiiaz.fireflies.entity;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {


    public static final EntityType<FireflyEntity> FIREFLY = register(
            "firefly", EntityType.Builder.create(FireflyEntity::new, SpawnGroup.CREATURE).dimensions(0.2F, 0.2F).eyeHeight(0.1F).maxTrackingRange(8)
    );

    private static <T extends Entity> EntityType<T> register(@SuppressWarnings("SameParameterValue") String id, EntityType.Builder<T> type) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, Mod.createIdentifier(id));
        return Registry.register(Registries.ENTITY_TYPE, key, type.build(key));
    }


    public static void initialize() {}


}
