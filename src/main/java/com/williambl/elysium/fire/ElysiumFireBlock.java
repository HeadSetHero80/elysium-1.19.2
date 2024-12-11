package com.williambl.elysium.fire;

import com.williambl.elysium.Elysium;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

public class ElysiumFireBlock extends AbstractFireBlock {
    public ElysiumFireBlock(AbstractBlock.Settings properties) {
        super(properties, 2.0F);
    }

    @NotNull
    public BlockState getStateForNeighborUpdate(@NotNull BlockState blockState, @NotNull Direction direction, @NotNull BlockState blockState2, @NotNull WorldAccess levelAccessor, @NotNull BlockPos blockPos, @NotNull BlockPos blockPos2) {
        return this.canPlaceAt(blockState, levelAccessor, blockPos) ? this.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    public boolean canPlaceAt(@NotNull BlockState blockState, WorldView levelReader, BlockPos blockPos) {
        return canSurviveOnBlock(levelReader.getBlockState(blockPos.down()));
    }

    public static boolean canSurviveOnBlock(BlockState blockState) {
        return blockState.isIn(Elysium.ELYSIUM_FIRE_BASE_BLOCKS);
    }

    protected boolean isFlammable(@NotNull BlockState blockState) {
        return true;
    }
}
