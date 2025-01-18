package io.github.diiiaz.fireflies.entity;

import io.github.diiiaz.fireflies.Mod;
import io.github.diiiaz.fireflies.entity.custom.FireflyEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    public static final Identifier id = Mod.createIdentifier("firefly");
    private static final RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);

    public static final EntityType<FireflyEntity> FIREFLY = Registry.register(Registries.ENTITY_TYPE, id,
            EntityType.Builder.create(FireflyEntity::new, SpawnGroup.AMBIENT)
                    .dimensions(0.2f, 0.2f)
                    .build(key));

    public static void initialize() {}


}
