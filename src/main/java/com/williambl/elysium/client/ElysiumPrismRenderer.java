package com.williambl.elysium.client;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.machine.prism.ElysiumPrismBlockEntity;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public class ElysiumPrismRenderer implements BlockEntityRenderer<ElysiumPrismBlockEntity> {
    private static final float[] ALPHA_BY_POWER = new float[]{0.0F, 0.1F, 0.25F, 0.5F, 1.0F};
    private static final Identifier BEAM_TEXTURE = Elysium.id("textures/entity/elysium_prism_beam.png");
    private static final Random RANDOM = Random.create();

    public void render(ElysiumPrismBlockEntity blockEntity, float partialTick, MatrixStack poseStack, @NotNull VertexConsumerProvider bufferSource, int packedLight, int packedOverlay) {
        poseStack.push();
        int value = blockEntity.getCachedState().get(ElysiumBlocks.ELYSIUM_POWER);
        BlockPos laserEnd = blockEntity.getLaserEnd();
        if (laserEnd != null && value > 0) {
            Direction dir = (Direction)blockEntity.getCachedState().get(FacingBlock.FACING);
            Vec3d rayStart = Vec3d.ofCenter(blockEntity.getPos());
            Vec3d rayEnd = Vec3d.ofCenter(laserEnd);
            int length = blockEntity.getPos().getManhattanDistance(laserEnd);
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.multiply(dir.getRotationQuaternion());
            poseStack.translate(-0.5D, -0.5D, -0.5D);
            if (value == 4) {
                poseStack.translate(RANDOM.nextGaussian() * 0.01D, 0.0D, RANDOM.nextGaussian() * 0.01D);
            }

            long time = blockEntity.getWorld() == null ? 0L : blockEntity.getWorld().getTime();
            float beamSizeWobble = blockEntity.getRenderingBeamWobble(partialTick);
            if (MinecraftClient.getInstance().cameraEntity != null) {
                renderLaser(poseStack, bufferSource, partialTick, length, time, beamSizeWobble, ALPHA_BY_POWER[value], Math.min(rayStart.distanceTo(MinecraftClient.getInstance().cameraEntity.getEyePos()), rayEnd.distanceTo(MinecraftClient.getInstance().cameraEntity.getEyePos())));
            }
        }

        poseStack.pop();
    }

    public boolean shouldRenderOffScreen(@NotNull ElysiumPrismBlockEntity blockEntity) {
        return true;
    }

    public int getRenderDistance() {
        return 256;
    }

    public static void renderLaser(MatrixStack poseStack, VertexConsumerProvider multiBufferSource, float partialTicks, int length, long time, float wobble, float alpha, double distanceToCamera) {
        renderBeaconBeam(poseStack, multiBufferSource, BEAM_TEXTURE, partialTicks, 1.0F, time, 0, length, new float[]{1.0F, 1.0F, 1.0F}, 0.2F + wobble * 0.05F, alpha, distanceToCamera);
    }

    public static void renderBeaconBeam(MatrixStack poseStack, VertexConsumerProvider bufferSource, Identifier beamLocation, float partialTick, float textureScale, long gameTime, int yOffset, int height, float[] colors, float beamRadius, float alpha, double distanceToCamera) {
        int i = yOffset + height;
        poseStack.push();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        float f = (float)Math.floorMod(gameTime, 80) + partialTick;
        float g = height < 0 ? f : -f;
        float h = MathHelper.fractionalPart(g * 0.2F - (float) MathHelper.floor(g * 0.1F));
        float j = colors[0];
        float k = colors[1];
        float l = colors[2];
        poseStack.multiply(new Quaternion(Vec3f.POSITIVE_Y.getDegreesQuaternion(f * 2.25F - 45.0F)));
        float q = -beamRadius;
        float t = -beamRadius;
        float w = -1.0F + h;
        float x = (float)height * textureScale * (0.5F / beamRadius) + w;
        float outsideAlpha = MathHelper.lerp((float)distanceToCamera / 7.0F, 0.0F, 0.25F);
        renderPart(poseStack, bufferSource.getBuffer(GlowEffectManager.getRenderType(RenderLayer.getBeaconBeam(beamLocation, false))), j, k, l, outsideAlpha, yOffset, i, 0.0F, beamRadius, beamRadius, 0.0F, q, 0.0F, 0.0F, t, 0.0F, 1.0F, x, w);
        renderPart(poseStack, bufferSource.getBuffer(RenderLayer.getBeaconBeam(beamLocation, true)), j, k, l, alpha, yOffset, i, 0.0F, beamRadius, beamRadius, 0.0F, q, 0.0F, 0.0F, t, 0.0F, 1.0F, x, w);
        poseStack.pop();
    }

    private static void renderPart(MatrixStack poseStack, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float x0, float z0, float x1, float z1, float x2, float z2, float x3, float z3, float minU, float maxU, float minV, float maxV) {
        MatrixStack.Entry pose = poseStack.peek();
        Matrix4f matrix4f = pose.getPositionMatrix();
        Matrix3f matrix3f = pose.getNormalMatrix();
        renderSideQuad(matrix4f, matrix3f, consumer, red, green, blue, alpha, minY, maxY, x0, z0, x1, z1, minU, maxU, minV, maxV);
        renderSideQuad(matrix4f, matrix3f, consumer, red, green, blue, alpha, minY, maxY, x3, z3, x2, z2, minU, maxU, minV, maxV);
        renderSideQuad(matrix4f, matrix3f, consumer, red, green, blue, alpha, minY, maxY, x1, z1, x3, z3, minU, maxU, minV, maxV);
        renderSideQuad(matrix4f, matrix3f, consumer, red, green, blue, alpha, minY, maxY, x2, z2, x0, z0, minU, maxU, minV, maxV);
        renderCapQuad(matrix4f, matrix3f, consumer, red, green, blue, alpha, maxY, x0, x1, x3, x2, z0, z1, z3, z2, minU, maxU, minV, maxV);
    }

    private static void renderSideQuad(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float red, float green, float blue, float alpha, int minY, int maxY, float minX, float minZ, float maxX, float maxZ, float minU, float maxU, float minV, float maxV) {
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxY, minX, minZ, maxU, minV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minY, minX, minZ, maxU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minY, maxX, maxZ, minU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxY, maxX, maxZ, minU, minV);
    }

    private static void renderCapQuad(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float red, float green, float blue, float alpha, int y, float x0, float x1, float x2, float x3, float z0, float z1, float z2, float z3, float minU, float maxU, float minV, float maxV) {
        addVertex(pose, normal, consumer, red, green, blue, alpha, y, x0, z0, maxU, minV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, y, x1, z1, maxU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, y, x2, z2, minU, maxV);
        addVertex(pose, normal, consumer, red, green, blue, alpha, y, x3, z3, minU, minV);
    }

    private static void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float red, float green, float blue, float alpha, int y, float x, float z, float u, float v) {
        consumer.vertex(pose, x, (float)y, z).color(red, green, blue, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, 0.0F, 1.0F, 0.0F).next();
    }
}