package io.github.diiiaz.fireflies.particle;

import io.github.diiiaz.fireflies.Mod;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModParticles {

    // This DefaultParticleType gets called when you want to use your particle in code.
    public static final SimpleParticleType FIREFLY = register(FabricParticleTypes.simple(), "firefly");

    private static SimpleParticleType register(SimpleParticleType particleTypes, String name) {
        // Register our custom particle type in the mod initializer.
        return Registry.register(Registries.PARTICLE_TYPE, Mod.createIdentifier(name), particleTypes);
    }

    public static void initialize() {}

}
