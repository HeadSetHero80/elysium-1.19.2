package com.williambl.elysium.client.particle;

import com.williambl.elysium.particles.ArcParticleOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArcParticle extends Particle {
    private static final int LIFETIME = 20;
    private final BufferBuilderStorage renderBuffers;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    @Nullable
    private final Entity entityToFollow;
    private final Vec3d offsetFromEntity;
    private final long seed;
    private float scale = 1.0F;

    protected ArcParticle(ClientWorld clientLevel, BufferBuilderStorage renderBuffers, double x, double y, double z, double targetX, double targetY, double targetZ, float scale, @Nullable Entity entityToFollow) {
        super(clientLevel, x, y, z);
        this.renderBuffers = renderBuffers;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.entityToFollow = entityToFollow;
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.maxAge = 20;
        this.seed = clientLevel.getTime();
        this.offsetFromEntity = entityToFollow == null ? Vec3d.ZERO : entityToFollow.getPos();
        this.scale(scale);
    }

    @NotNull
    public Particle scale(float scale) {
        this.scale = scale;
        return super.scale(scale);
    }

    @NotNull
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    public void tick() {
        super.tick();
    }

    public void buildGeometry(@NotNull VertexConsumer immediateBuffer, Camera renderInfo, float partialTicks) {
        VertexConsumerProvider.Immediate bufferSource = this.renderBuffers.getEntityVertexConsumers();
        VertexConsumer buffer = bufferSource.getBuffer(RenderLayer.getLightning());
        MatrixStack poseStack = new MatrixStack();
        poseStack.push();
        poseStack.translate(this.x - renderInfo.getPos().x, this.y - renderInfo.getPos().y, this.z - renderInfo.getPos().z);
        if (this.entityToFollow != null) {
            Vec3d offset = this.entityToFollow.getLerpedPos(partialTicks).subtract(this.offsetFromEntity);
            poseStack.translate(offset.getX(), offset.getY(), offset.getZ());
        }

        poseStack.scale(this.scale, this.scale, this.scale);
        Vec3d targetVec = (new Vec3d(this.targetX - this.x, this.targetY - this.y, this.targetZ - this.z)).multiply(1.0D / (double)this.scale);
        if (this.age % 4 != 0) {
            Random random = Random.create(this.seed + (long)(this.age / 9));
            float[][] nodePositions = generateNodePositions(random, targetVec);
            drawLightning(poseStack, buffer, nodePositions, random, 0, 0, this.getMinTreeDepth(partialTicks), this.getMaxTreeDepth(partialTicks));
        }

        poseStack.pop();
        bufferSource.draw();
    }

    private static void drawLightning(MatrixStack poseStack, VertexConsumer buffer, float[][] nodePositions, Random random, int recursiveDepth, int treeDepth, int minTreeDepth, int maxTreeDepth) {
        if (recursiveDepth <= 2) {
            for(int i = 0; i < nodePositions.length - 1; ++i) {
                float[] pos = nodePositions[i];
                float[] nextPos = nodePositions[i + 1];
                if (treeDepth <= maxTreeDepth && treeDepth >= minTreeDepth) {
                    drawCuboid(poseStack.peek().getPositionMatrix(), buffer, pos[0], pos[1], pos[2], nextPos[0], nextPos[1], nextPos[2], 0.05F, 0.4F);
                    drawCuboid(poseStack.peek().getPositionMatrix(), buffer, pos[0], pos[1], pos[2], nextPos[0], nextPos[1], nextPos[2], 0.1F, 0.06F);
                }

                ++treeDepth;
                if (i > 0 && i < nodePositions.length - 2 && random.nextFloat() < 0.4F) {
                    Vec3d newTarget = (new Vec3d((double)pos[0] * 0.5D + (double)random.nextFloat(), (double)pos[1] * 0.5D + (double)random.nextFloat(), (double)pos[2] * 0.5D + (double)random.nextFloat())).multiply(5.0D);
                    float[][] newNodePositions = generateNodePositions(random, newTarget);
                    poseStack.push();
                    poseStack.translate(pos[0], pos[1], pos[2]);
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    drawLightning(poseStack, buffer, newNodePositions, random, recursiveDepth + 1, treeDepth, minTreeDepth, maxTreeDepth);
                    poseStack.pop();
                }
            }

        }
    }

    private static float[][] generateNodePositions(Random random, Vec3d target) {
        Vec3d perpendicular1 = target.crossProduct(new Vec3d(1.0D, 0.0D, 0.0D)).normalize();
        Vec3d perpendicular2 = target.crossProduct(perpendicular1).normalize();
        float[][] nodePositions = new float[random.nextBetweenExclusive(6, 8)][];

        for(int i = 0; i < nodePositions.length; ++i) {
            float progress = (float)i / (float)nodePositions.length;
            float maxMovement = 1.0F / (float)nodePositions.length;
            if (i == 0) {
                nodePositions[i] = new float[]{0.0F, 0.0F, 0.0F};
            } else if (i == nodePositions.length - 1) {
                nodePositions[i] = new float[]{(float)target.getX(), (float)target.getY(), (float)target.getZ()};
            } else {
                Vec3d offset = perpendicular1.multiply((double)random.nextFloat()).add(perpendicular2.multiply((double)random.nextFloat())).add(target.multiply((double)(progress + random.nextFloat() * maxMovement)));
                nodePositions[i] = new float[]{(float)offset.getX(), (float)offset.getY(), (float)offset.getZ()};
            }
        }

        return nodePositions;
    }

    private static void drawCuboid(Matrix4f matrix4f, VertexConsumer vertexConsumer, float xStart, float yStart, float zStart, float xEnd, float yEnd, float zEnd, float radius, float alpha) {
        drawQuad(vertexConsumer, matrix4f, xStart + radius, xStart + radius, xEnd + radius, xEnd + radius, yStart, yEnd, zStart + radius, zStart - radius, zEnd - radius, zEnd + radius, alpha);
        drawQuad(vertexConsumer, matrix4f, xStart - radius, xStart - radius, xEnd - radius, xEnd - radius, yStart, yEnd, zStart + radius, zStart - radius, zEnd - radius, zEnd + radius, alpha);
        drawQuad(vertexConsumer, matrix4f, xStart + radius, xStart - radius, xEnd - radius, xEnd + radius, yStart, yEnd, zStart + radius, zStart + radius, zEnd + radius, zEnd + radius, alpha);
        drawQuad(vertexConsumer, matrix4f, xStart + radius, xStart - radius, xEnd - radius, xEnd + radius, yStart, yEnd, zStart - radius, zStart - radius, zEnd - radius, zEnd - radius, alpha);
    }

    private static void drawQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, float x1, float x2, float x3, float x4, float y1, float y2, float z1, float z2, float z3, float z4, float alpha) {
        drawSingleSidedQuad(vertexConsumer, matrix4f, x1, x2, x3, x4, y1, y2, z1, z2, z3, z4, alpha);
        drawSingleSidedQuad(vertexConsumer, matrix4f, x4, x3, x2, x1, y2, y1, z4, z3, z2, z1, alpha);
    }

    private static void drawSingleSidedQuad(VertexConsumer vertexConsumer, Matrix4f matrix4f, float x1, float x2, float x3, float x4, float y1, float y2, float z1, float z2, float z3, float z4, float alpha) {
        vertexConsumer.vertex(matrix4f, x1, y1, z1).color(0.7F, 0.8F, 1.0F, alpha).next();
        vertexConsumer.vertex(matrix4f, x2, y1, z2).color(0.7F, 0.8F, 1.0F, alpha).next();
        vertexConsumer.vertex(matrix4f, x3, y2, z3).color(0.7F, 0.8F, 1.0F, alpha).next();
        vertexConsumer.vertex(matrix4f, x4, y2, z4).color(0.7F, 0.8F, 1.0F, alpha).next();
    }

    private int getMaxTreeDepth(float tickDelta) {
        return (int)Math.min(5.0F, ((float)this.age + tickDelta) * 1.3F);
    }

    private int getMinTreeDepth(float tickDelta) {
        return (int)Math.max(0.0F, 0.65F * ((float)this.age + tickDelta) - 5.0F);
    }

    public int getBrightness(float partialTick) {
        return 240;
    }

    public static class Provider implements ParticleFactory<ArcParticleOption> {
        public Particle createParticle(ArcParticleOption type, @NotNull ClientWorld level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ArcParticle particle = new ArcParticle(level, MinecraftClient.getInstance().getBufferBuilders(), x, y, z, type.targetX(), type.targetY(), type.targetZ(), type.scale(), level.getEntityById(type.entityId()));
            particle.setVelocity(0.0D, 0.0D, 0.0D);
            particle.setColor(1.0F, 1.0F, 1.0F);
            return particle;
        }
    }
}