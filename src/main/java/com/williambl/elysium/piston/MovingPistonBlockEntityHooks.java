package com.williambl.elysium.piston;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface MovingPistonBlockEntityHooks {
    void elysium$setMovingBlockEntityTag(NbtCompound var1);

    @Nullable
    NbtCompound elysium$getMovingBlockEntityTag();
}