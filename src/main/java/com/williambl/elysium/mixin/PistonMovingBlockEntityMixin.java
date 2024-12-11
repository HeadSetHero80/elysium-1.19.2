package com.williambl.elysium.mixin;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.piston.MovingPistonBlockEntityHooks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements MovingPistonBlockEntityHooks {

    @Unique
    private NbtCompound movingBlockEntityTag;

    public PistonMovingBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = "finish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V"
            )
    )
    private void elysium$moveBEOnFinish(CallbackInfo info) {
        moveBE(this.world, (PistonBlockEntity) (Object) this);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V"
            )
    )
    private static void elysium$moveBEOnTick(World world, BlockPos pos, BlockState state, PistonBlockEntity blockEntity, CallbackInfo info) {
        moveBE(world, blockEntity);
    }

    @Unique
    private static void moveBE(World world, PistonBlockEntity pistonBlockEntity) {
        BlockPos pos = pistonBlockEntity.getPos();
        Elysium.LOGGER.trace("Possibly going to move a block entity @ {} with {}", pos, pistonBlockEntity);

        if (world == null) {
            Elysium.LOGGER.trace("Not moving a block entity: world is null.");
            return;
        }

        if (world.isClient) {
            Elysium.LOGGER.trace("Not moving a block entity: client-side operation.");
            return;
        }

        NbtCompound tag = ((MovingPistonBlockEntityHooks) pistonBlockEntity).elysium$getMovingBlockEntityTag();
        if (tag == null) {
            Elysium.LOGGER.trace("Not moving a block entity: tag is null.");
            return;
        }

        BlockState blockState = world.getBlockState(pos);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null) {
            Elysium.LOGGER.trace("Not moving a block entity: no BlockEntity at {}", pos);
            return;
        }

        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());

        blockEntity.readNbt(tag);
        ((ServerWorld) world).getChunkManager().markForUpdate(pos);
    }

    @Override
    public void elysium$setMovingBlockEntityTag(NbtCompound tag) {
        Elysium.LOGGER.trace("Setting moving block entity tag: {}", tag);
        this.movingBlockEntityTag = tag;
    }

    @Override
    @Nullable
    public NbtCompound elysium$getMovingBlockEntityTag() {
        return this.movingBlockEntityTag;
    }

    @Inject(
            method = "writeNbt",
            at = @At("TAIL")
    )
    private void elysium$saveBETag(NbtCompound nbt, CallbackInfo ci) {
        if (this.movingBlockEntityTag != null) {
            nbt.put("elysium$movingBlockEntityTag", this.movingBlockEntityTag);
        }
    }

    @Inject(
            method = "readNbt",
            at = @At("TAIL")
    )
    private void elysium$loadBETag(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("elysium$movingBlockEntityTag")) {
            this.movingBlockEntityTag = nbt.getCompound("elysium$movingBlockEntityTag");
        }
    }
}