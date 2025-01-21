package io.github.diiiaz.fireflies.utils;

import io.github.diiiaz.fireflies.Mod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.poi.PointOfInterestType;

public class ModTags {


    public static class EntityTypes {


        public static final TagKey<EntityType<?>> ALCOVE_INHABITORS = createTag("alcove_inhabitors");


        private static TagKey<EntityType<?>> createTag(@SuppressWarnings("SameParameterValue") String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Mod.createIdentifier(name));
        }

    }


    public static class Enchantments {

        public static final TagKey<Enchantment> PREVENTS_FIREFLY_SPAWNS_WHEN_MINING = createTag("prevents_firefly_spawns_when_mining");


        private static TagKey<Enchantment> createTag(@SuppressWarnings("SameParameterValue") String name) {
            return TagKey.of(RegistryKeys.ENCHANTMENT, Mod.createIdentifier(name));
        }

    }

    public static class PointOfInterestTypes {


        public static final TagKey<PointOfInterestType> FIREFLY_HOME = createTag("firefly_home");


        private static TagKey<PointOfInterestType> createTag(@SuppressWarnings("SameParameterValue") String name) {
            return TagKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Mod.createIdentifier(name));
        }

    }

    public static void initialize() {}

}
