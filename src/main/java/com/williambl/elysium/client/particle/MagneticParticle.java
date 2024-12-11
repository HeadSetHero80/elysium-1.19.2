package com.williambl.elysium.client.particle;

import com.williambl.elysium.particles.MagneticWaveParticleOption;
import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class MagneticParticle extends SpriteBillboardParticle {
    private static final Vec3f ROTATION_VECTOR = (Vec3f) Util.make(new Vec3f(0.5F, 0.5F, 0.5F), Vec3f::normalize);
    private final SpriteProvider sprites;
    private final float yRot;
    private final float xRot;
    private final float targetScale;
    private final float initialScale;
    protected final boolean isReversed;
    private float prevQuadSize;
    private float prevAlpha;

    public MagneticParticle(ClientWorld clientLevel, SpriteProvider sprites, float yRot, float xRot, double d, double e, double f, boolean isReversed, float initialScale, float targetScale, int lifetime) {
        super(clientLevel, d, e, f);
        this.yRot = yRot;
        this.xRot = xRot;
        this.targetScale = targetScale;
        this.gravityStrength = 0.0F;
        this.sprites = sprites;
        this.setSpriteForAge(sprites);
        this.collidesWithWorld = false;
        this.maxAge = lifetime;
        this.velocityMultiplier = 1.0F;
        this.isReversed = isReversed;
        this.initialScale = initialScale;
        this.scale = isReversed ? this.targetScale : this.initialScale;
        this.prevQuadSize = this.scale;
        this.setAlpha(isReversed ? 0.0F : 1.0F);
        this.prevAlpha = this.alpha;
    }

    @NotNull
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        this.setSpriteForAge(this.sprites);
        this.prevAlpha = this.alpha;
        this.prevQuadSize = this.scale;
        float delta = this.getDelta();
        this.scale = MathHelper.lerp(1.0F - delta, this.initialScale, this.targetScale);
        this.setBoundingBoxSpacing(0.2F * this.scale, 0.2F * this.scale);
        this.setAlpha(delta);
    }

    public void buildGeometry(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
        this.renderRotatedParticle(buffer, renderInfo, partialTicks, (quaternion) -> {
            quaternion.hamiltonProduct(makeQuaternion(Vec3f.POSITIVE_Y, this.yRot, false));
            quaternion.hamiltonProduct(makeQuaternion(Vec3f.POSITIVE_X, this.xRot, false));
        });
        this.renderRotatedParticle(buffer, renderInfo, partialTicks, (quaternion) -> {
            quaternion.hamiltonProduct(makeQuaternion(Vec3f.POSITIVE_Y, this.yRot - (float) Math.PI, false));
            quaternion.hamiltonProduct(makeQuaternion(Vec3f.POSITIVE_X, -this.xRot, false));
        });
    }

    protected float getDelta() {
        return (float)(1.0D - Math.pow(this.isReversed ? (double)(this.maxAge - this.age) : (double)this.age, 3.0D) / Math.pow((double)this.maxAge, 3.0D));
    }

    public float getSize(float partialTick) {
        return MathHelper.lerp(partialTick, this.prevQuadSize, this.scale);
    }

    public int getBrightness(float partialTick) {
        return 240;
    }

    private void renderRotatedParticle(VertexConsumer consumer, Camera camera, float f, Consumer<Quaternion> quaternion) {
        Vec3d cameraPos = camera.getPos();
        float x = (float)(MathHelper.lerp(f, this.prevPosX, this.x) - cameraPos.getX()) + (float)this.random.nextGaussian() * 0.01F;
        float y = (float)(MathHelper.lerp(f, this.prevPosY, this.y) - cameraPos.getY()) + (float)this.random.nextGaussian() * 0.01F;
        float z = (float)(MathHelper.lerp(f, this.prevPosZ, this.z) - cameraPos.getZ()) + (float)this.random.nextGaussian() * 0.01F;

        Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
        quaternion.accept(rotation);

        Vec3f[] vertices = new Vec3f[]{
                new Vec3f(-1.0F, -1.0F, 0.0F),
                new Vec3f(-1.0F, 1.0F, 0.0F),
                new Vec3f(1.0F, 1.0F, 0.0F),
                new Vec3f(1.0F, -1.0F, 0.0F)
        };

        float size = this.getSize(f);

        for (int i = 0; i < 4; ++i) {
            Vec3f vertex = vertices[i];
            vertex.rotate(rotation);
            vertex.scale(size * this.targetScale);
            vertex.add(x, y, z);
        }

        int light = this.getBrightness(f);
        float alpha = MathHelper.lerp(f, this.prevAlpha, this.alpha);
        this.makeCornerVertex(consumer, vertices[0], this.getMaxU(), this.getMaxV(), light, alpha);
        this.makeCornerVertex(consumer, vertices[1], this.getMaxU(), this.getMinV(), light, alpha);
        this.makeCornerVertex(consumer, vertices[2], this.getMinU(), this.getMinV(), light, alpha);
        this.makeCornerVertex(consumer, vertices[3], this.getMinU(), this.getMaxV(), light, alpha);
    }

    public static Quaternion makeQuaternion(Vec3f axis, float angleDegrees, boolean degrees) {
        if (degrees) {
            angleDegrees *= (float) (Math.PI / 180.0);
        }
        float sin = MathHelper.sin(angleDegrees / 2.0F);
        float cos = MathHelper.cos(angleDegrees / 2.0F);
        return new Quaternion(axis.getX() * sin, axis.getY() * sin, axis.getZ() * sin, cos);
    }

    private void makeCornerVertex(VertexConsumer consumer, Vec3f vertex, float u, float v, int light, float alpha) {
        consumer.vertex(vertex.getX(), vertex.getY(), vertex.getZ())
                .texture(u, v)
                .color(this.red, this.green, this.blue, alpha)
                .light(light)
                .next();
    }

    public static class Provider implements ParticleFactory<MagneticWaveParticleOption> {
        private final SpriteProvider sprites;

        public Provider(SpriteProvider spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(MagneticWaveParticleOption type, @NotNull ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            double speed = Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed + zSpeed * zSpeed);
            int lifetime = (int)Math.ceil((double)type.distance() / speed);
            MagneticParticle particle = new MagneticParticle(level, this.sprites, (float)MathHelper.atan2(xSpeed, zSpeed), (float)MathHelper.atan2(ySpeed, -Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed)), x, y, z, type.isReversed(), 0.5F, type.widthScale(), lifetime);
            particle.setVelocity(xSpeed, ySpeed, zSpeed);
            particle.setColor(1.0F, 1.0F, 1.0F);
            return particle;
        }
    }

    public static class SimpleProvider implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public SimpleProvider(SpriteProvider spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(@NotNull DefaultParticleType type, @NotNull ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            MagneticParticle particle = new MagneticParticle(level, this.sprites, (float)MathHelper.atan2(xSpeed, zSpeed), (float)MathHelper.atan2(ySpeed, -Math.sqrt(xSpeed * xSpeed + zSpeed * zSpeed)), x, y, z, false, 0.5F, 1.75F, 25);
            particle.setVelocity(xSpeed, ySpeed, zSpeed);
            particle.setColor(1.0F, 1.0F, 1.0F);
            return particle;
        }
    }
}