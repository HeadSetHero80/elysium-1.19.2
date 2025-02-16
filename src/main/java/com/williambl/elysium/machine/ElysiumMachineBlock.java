package com.williambl.elysium.machine;

import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public abstract class ElysiumMachineBlock extends BlockWithEntity {
    protected ElysiumMachineBlock(AbstractBlock.Settings properties) {
        super(properties);
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(Properties.FACING, Direction.NORTH)
                .with(ElysiumBlocks.ELYSIUM_POWER, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING, ElysiumBlocks.ELYSIUM_POWER);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction dir = context.getPlayerLookDirection().getOpposite();
        BlockState partialState = this.getDefaultState().with(Properties.FACING, dir);
        return partialState.with(ElysiumBlocks.ELYSIUM_POWER, this.getPower(context.getWorld(), context.getBlockPos(), partialState));
    }

    @NotNull
    @Override
    public BlockRenderType getRenderType(@NotNull BlockState blockState) {
        return BlockRenderType.MODEL;
    }

    @NotNull
    @Override
    public BlockState getStateForNeighborUpdate(@NotNull BlockState state, @NotNull Direction direction,
                                                @NotNull BlockState neighborState, @NotNull WorldAccess level,
                                                @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        int power = this.getPower(level, currentPos, state);
        return state.with(ElysiumBlocks.ELYSIUM_POWER, power);
    }

    public int getPower(WorldAccess level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        int beamPower;

        if (blockEntity instanceof BeamPowered) {
            BeamPowered beamPowered = (BeamPowered) blockEntity;
            beamPower = beamPowered.getBeamPower(level);
        } else {
            beamPower = 0;
        }

        return Math.max(beamPower, Arrays.stream(Direction.values())
                .filter(d -> this.isReceivingSide(state, d))
                .map(pos::offset)
                .map(level::getBlockState)
                .filter(s -> !s.contains(Properties.LIT) || s.get(Properties.LIT))
                .map(AbstractBlock.AbstractBlockState::getBlock)
                .map(ElysiumBlocks.PRISM_POWERS::get)
                .mapToInt(i -> i != null ? i : 0) // FIX: Handle null values
                .max()
                .orElse(0));
    } // <-- Correctly closed getPower method

    public boolean isReceivingSide(BlockState state, Direction side) {
        return side == state.get(Properties.FACING).getOpposite();
    }
}
