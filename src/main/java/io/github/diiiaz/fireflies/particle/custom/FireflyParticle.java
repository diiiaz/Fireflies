package io.github.diiiaz.fireflies.particle.custom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FireflyParticle extends SpriteBillboardParticle {

    Vec3d wantedPosition = Vec3d.ZERO;

    FireflyParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.gravityStrength = 0.0F;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.scale *= 0.75F;
        this.maxAge = this.random.nextBetween(50, 100);
        this.setSpriteForAge(spriteProvider);
        this.setAlpha(0.0F);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getBrightness(float tint) {
        return 240;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (wantedPosition == Vec3d.ZERO) {
            wantedPosition = new Vec3d(
                MathHelper.map(this.random.nextFloat(), 0, 1, getBoundingBox().minX, getBoundingBox().maxX),
                MathHelper.map(this.random.nextFloat(), 0, 1, getBoundingBox().minY, getBoundingBox().maxY),
                MathHelper.map(this.random.nextFloat(), 0, 1, getBoundingBox().minZ, getBoundingBox().maxZ)
            );
        }
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            float i = this.maxAge - this.age;
            float d = 1.0F / i;
            this.x = MathHelper.lerp(d, this.x, wantedPosition.x);
            this.y = MathHelper.lerp(d, this.y, wantedPosition.y);
            this.z = MathHelper.lerp(d, this.z, wantedPosition.z);

            double agePercentage = MathHelper.map(this.age, 0, this.maxAge, 0, 1);
            float alpha = (float) (4 * agePercentage * (1 - agePercentage));
            this.setAlpha(alpha);
        }
    }

    public void move(double dx, double dy, double dz) {
        this.setBoundingBox(this.getBoundingBox().offset(dx, dy, dz));
        this.repositionFromBoundingBox();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new FireflyParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
