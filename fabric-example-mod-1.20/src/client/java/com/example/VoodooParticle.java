package com.example;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class VoodooParticle extends SpriteBillboardParticle {

    // 1. ОГОЛОШУЄМО ЗМІННУ (цього не вистачало)
    private final float rotationSpeed;

    protected VoodooParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);

        // ВИПРАВЛЕНО: тепер бере випадковий символ
        this.setSprite(spriteProvider.getSprite(world.random));

        this.maxAge = 40 + world.random.nextInt(20);
        this.scale = 0.5f;
        this.velocityMultiplier = 0.9f;

        this.red = 1.0f;
        this.green = 1.0f;
        this.blue = 1.0f;

        this.angle = world.random.nextFloat() * 6.28f;
        this.rotationSpeed = (world.random.nextFloat() - 0.5f) * 0.2f;
    }

    // 3. ДОДАЄМО ТІК (щоб символ крутився кожного кадру)
    @Override
    public void tick() {
        super.tick();
        this.prevAngle = this.angle;
        this.angle += this.rotationSpeed;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            return new VoodooParticle(world, x, y, z, this.spriteProvider, velocityX, velocityY, velocityZ);
        }
    }
}