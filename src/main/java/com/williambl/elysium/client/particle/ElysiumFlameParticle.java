package com.williambl.elysium.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class ElysiumFlameParticle extends SpriteBillboardParticle {
    protected ElysiumFlameParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.setSprite(spriteProvider); // Set the sprite for the particle
        this.scale = 0.1F; // Set the size of the particle
        this.maxAge = 20; // Set the lifetime of the particle
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;
        this.setColor(1.0F, 0.5F, 0.0F); // Set the color of the particle (red, green, blue)
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE; // Use the opaque texture sheet
    }

    @Override
    public void tick() {
        super.tick();
        // Custom behavior for the particle (e.g., fade out over time)
        this.setAlpha(1.0F - (float) this.age / this.maxAge); // Fade out over time
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new ElysiumFlameParticle(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteProvider);
        }
    }
}