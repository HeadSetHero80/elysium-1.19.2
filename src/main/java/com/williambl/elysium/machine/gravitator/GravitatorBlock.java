package com.williambl.elysium.machine.gravitator;

import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravitatorBlock extends ElysiumMachineBlock {
    public final boolean isOutwards;

    public GravitatorBlock(AbstractBlock.Settings properties, boolean isOutwards) {
        super(properties);
        this.isOutwards = isOutwards;
    }

    @Nullable
    public BlockEntity createBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return ElysiumBlocks.GRAVITATOR_BE.instantiate(blockPos, blockState);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull World level, @NotNull BlockState blockState, @NotNull BlockEntityType<T> blockEntityType) {
        return checkType(blockEntityType, ElysiumBlocks.GRAVITATOR_BE, (l, p, s, e) -> GravitatorBlockEntity.tick(l, p, s, e, this.isOutwards));
    }
}