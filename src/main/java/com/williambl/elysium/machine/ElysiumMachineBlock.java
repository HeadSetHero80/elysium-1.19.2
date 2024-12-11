package com.williambl.elysium.machine;

import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public abstract class ElysiumMachineBlock extends BlockWithEntity {
    protected ElysiumMachineBlock(AbstractBlock.Settings properties) {
        super(properties);
        this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(Properties.FACING, Direction.NORTH)).with(ElysiumBlocks.ELYSIUM_POWER, 0));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{Properties.FACING}).add(new Property[]{ElysiumBlocks.ELYSIUM_POWER});
    }

    public BlockState getPlacementState(ItemPlacementContext context) {
        Direction dir = context.getPlayerLookDirection().getOpposite();
        BlockState partialState = (BlockState)this.getDefaultState().with(Properties.FACING, dir);
        return (BlockState)partialState.with(ElysiumBlocks.ELYSIUM_POWER, this.getPower(context.getWorld(), context.getBlockPos(), partialState));
    }

    @NotNull
    public BlockRenderType getRenderType(@NotNull BlockState blockState) {
        return BlockRenderType.MODEL;
    }

    @NotNull
    public BlockState getStateForNeighborUpdate(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull WorldAccess level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        int power = this.getPower(level, currentPos, state);
        return (BlockState)state.with(ElysiumBlocks.ELYSIUM_POWER, power);
    }

    public int getPower(WorldAccess level, BlockPos pos, BlockState state) {
        BlockEntity var6 = level.getBlockEntity(pos);
        int beamPower;
        if (var6 instanceof BeamPowered) {
            BeamPowered beamPowered = (BeamPowered)var6;
            beamPower = beamPowered.getBeamPower(level);
        } else {
            beamPower = 0;
        }

        return Math.max(beamPower, Arrays.stream(Direction.values()).filter((d) -> this.isReceivingSide(state, d)).map(pos::offset).map(level::getBlockState).filter((s) -> !s.contains(Properties.LIT) || s.get(Properties.LIT)).map(AbstractBlock.AbstractBlockState::getBlock).map(ElysiumBlocks.PRISM_POWERS::get).filter(Optional::isPresent).mapToInt(Optional::get).max().orElse(0));
    }

    public boolean isReceivingSide(BlockState state, Direction side) {
        return side == ((Direction)state.get(Properties.FACING)).getOpposite();
    }
}