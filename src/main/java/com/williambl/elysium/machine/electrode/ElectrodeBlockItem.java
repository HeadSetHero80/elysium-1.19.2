package com.williambl.elysium.machine.electrode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrodeBlockItem extends BlockItem {
    public ElectrodeBlockItem(Block block, Item.Settings properties) {
        super(block, properties);
    }

    protected boolean postPlacement(@NotNull BlockPos pos, @NotNull World level, @Nullable PlayerEntity player, @NotNull ItemStack stack, @NotNull BlockState state) {
        boolean result = super.postPlacement(pos, level, player, stack, state);
        if (!level.isClient() && player != null) {
            BlockEntity var8 = level.getBlockEntity(pos);
            if (var8 instanceof ElectrodeBlockEntity) {
                ElectrodeBlockEntity be = (ElectrodeBlockEntity)var8;
                be.setOwner(player.getUuid());
                return true;
            }
        }

        return result;
    }
}
