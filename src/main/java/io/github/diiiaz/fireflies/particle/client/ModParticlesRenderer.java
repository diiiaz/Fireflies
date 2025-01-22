package io.github.diiiaz.fireflies.particle.client;

import io.github.diiiaz.fireflies.particle.ModParticleTypes;
import io.github.diiiaz.fireflies.particle.custom.FireflyParticle;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class ModParticlesRenderer {


    public static void initialize() {

        ParticleFactoryRegistry.getInstance().register(ModParticleTypes.FIREFLY, FireflyParticle.Factory::new);

    }

}
