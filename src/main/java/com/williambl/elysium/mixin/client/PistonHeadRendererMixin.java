package com.williambl.elysium.mixin.client;

import com.williambl.elysium.piston.MovingPistonBlockEntityHooks;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.PistonBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PistonBlockEntityRenderer.class})
public class PistonHeadRendererMixin {
    @Inject(
            method = {"render(Lnet/minecraft/block/entity/PistonBlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/block/entity/PistonBlockEntityRenderer;renderModel(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;ZI)V",
                    ordinal = 2
            )}
    )
    private void elysium$renderMovingBlockEntity(PistonBlockEntity blockEntity, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (blockEntity.getPushedBlock().hasBlockEntity()) {
            BlockEntityRenderDispatcher blockEntityRenderDispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
            BlockEntity movedBE = ((BlockEntityProvider)blockEntity.getPushedBlock().getBlock()).createBlockEntity(blockEntity.getPos(), blockEntity.getPushedBlock());
            if (movedBE != null && blockEntityRenderDispatcher.get(movedBE) != null) {
                World level = blockEntity.getWorld();
                if (level != null) {
                    movedBE.setWorld(level);
                }

                NbtCompound savedTag = ((MovingPistonBlockEntityHooks)blockEntity).elysium$getMovingBlockEntityTag();
                if (savedTag != null) {
                    movedBE.readNbt(savedTag);
                }

                blockEntityRenderDispatcher.render(movedBE, partialTick, poseStack, bufferSource);
            }
        }

    }
}
