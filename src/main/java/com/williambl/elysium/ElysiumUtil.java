package com.williambl.elysium;

import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;
import java.util.Random;

public class ElysiumUtil {
    public static Optional<Item> getItemForEntity(Entity entity) {
        if (entity instanceof ItemEntity) {
            ItemEntity iE = (ItemEntity)entity;
            return Optional.of(iE.getStack().getItem());
        } else if (entity instanceof FallingBlockEntity) {
            FallingBlockEntity falling = (FallingBlockEntity)entity;
            return Optional.of(falling.getBlockState().getBlock().asItem());
        } else {
            return Optional.ofNullable(entity.getPickBlockStack()).map(ItemStack::getItem);
        }
    }

    public static Vec3d getRandomOrthogonal(Direction dir, Random random) {
        Vec3i normal = dir.getVector();
        return new Vec3d(normal.getX() == 0 ? random.nextDouble() - 0.5D : 0.0D, normal.getY() == 0 ? random.nextDouble() - 0.5D : 0.0D, normal.getZ() == 0 ? random.nextDouble() - 0.5D : 0.0D);
    }
}