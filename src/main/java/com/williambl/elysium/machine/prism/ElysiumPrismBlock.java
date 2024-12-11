package com.williambl.elysium.machine.prism;

import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElysiumPrismBlock extends ElysiumMachineBlock implements Stainable {
    private static final int[] SIGNAL_BY_POWER = new int[]{0, 3, 7, 11, 15};

    public ElysiumPrismBlock(AbstractBlock.Settings properties) {
        super(properties);
    }

    public void onStateReplaced(@NotNull BlockState state, World level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ElysiumPrismBlockEntity) {
            ElysiumPrismBlockEntity ePBE = (ElysiumPrismBlockEntity)be;
            if (level instanceof ServerWorld) {
                ePBE.resetPower();
            }
        }

        super.onStateReplaced(state, level, pos, newState, isMoving);
    }

    @Nullable
    public BlockEntity createBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ElysiumPrismBlockEntity(blockPos, blockState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return blockEntityType == ElysiumBlocks.ELYSIUM_PRISM_BLOCK_ENTITY ? (level instanceof ServerWorld ? (l, p, s, e) -> ElysiumPrismBlockEntity.tick(l, p, s, (ElysiumPrismBlockEntity)e) : (l, p, s, e) -> ElysiumPrismBlockEntity.clientTick(l, p, s, (ElysiumPrismBlockEntity)e)) : null;
    }

    public boolean isReceivingSide(BlockState state, Direction side) {
        return side != state.get(Properties.FACING);
    }

    public boolean hasComparatorOutput(@NotNull BlockState state) {
        return true;
    }

    public int getComparatorOutput(BlockState state, @NotNull World level, @NotNull BlockPos pos) {
        return SIGNAL_BY_POWER[state.get(ElysiumBlocks.ELYSIUM_POWER)];
    }

    public static int lightLevel(BlockState state) {
        return (int)((double)((Integer)state.get(ElysiumBlocks.ELYSIUM_POWER)).intValue() * 3.75D);
    }

    @NotNull
    public DyeColor getColor() {
        return DyeColor.WHITE;
    }
}