package com.williambl.elysium.machine.electrode;

import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrodeBlock extends ElysiumMachineBlock {
    public static final IntProperty CHARGES = IntProperty.of("charges", 0, 4);
    public static final BooleanProperty HAS_ROD = BooleanProperty.of("has_rod");

    public ElectrodeBlock(AbstractBlock.Settings properties) {
        super(properties);
        this.setDefaultState((BlockState)((BlockState)this.getDefaultState().with(CHARGES, 0)).with(HAS_ROD, false));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(new Property[]{CHARGES}).add(new Property[]{HAS_ROD});
    }

    @Nullable
    public BlockEntity createBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ElysiumBlocks.ELECTRODE_BE.instantiate(blockPos, blockState);
    }

    public void onStateReplaced(BlockState state, @NotNull World level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof Inventory) {
                ItemScatterer.spawn(level, pos, (Inventory)blockEntity);
                level.updateComparators(pos, this);
            }

            super.onStateReplaced(state, level, pos, newState, isMoving);
        }

    }

    @NotNull
    public BlockState getStateForNeighborUpdate(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull WorldAccess level, @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
        BlockState res = super.getStateForNeighborUpdate(state, direction, neighborState, level, currentPos, neighborPos);
        return direction == state.get(Properties.FACING) ? (BlockState)res.with(HAS_ROD, neighborState.isIn(ElysiumBlocks.LIGHTNING_RODS)) : res;
    }

    public void randomDisplayTick(BlockState state, @NotNull World level, @NotNull BlockPos pos, Random random) {
        if (random.nextBetweenExclusive(0, 3) < state.get(ElysiumBlocks.ELYSIUM_POWER)) {
            Vec3i dir = ((Direction)state.get(Properties.FACING)).getVector();
            level.addParticle(ParticleTypes.ELECTRIC_SPARK, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (double)dir.getX() * 0.2D, (double)dir.getY() * 0.2D, (double)dir.getZ() * 0.2D);
        }

    }

    @NotNull
    public ActionResult onUse(@NotNull BlockState state, @NotNull World level, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand hand, @NotNull BlockHitResult hitResult) {
        if (hitResult.getSide() == state.get(Properties.FACING)) {
            Item eBe = player.getStackInHand(hand).getItem();
            if (eBe instanceof BlockItem) {
                BlockItem blockItem = (BlockItem)eBe;
                if (blockItem.getBlock().getDefaultState().isIn(ElysiumBlocks.LIGHTNING_RODS)) {
                    return ActionResult.PASS;
                }
            }
        }

        if (level.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElectrodeBlockEntity) {
                ElectrodeBlockEntity eBe = (ElectrodeBlockEntity)be;
                if (eBe.canBeUsedBy(player)) {
                    player.openHandledScreen(eBe);
                }
            }

            return ActionResult.CONSUME;
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return level instanceof ServerWorld ? checkType(blockEntityType, ElysiumBlocks.ELECTRODE_BE, ElectrodeBlockEntity::tick) : null;
    }
}