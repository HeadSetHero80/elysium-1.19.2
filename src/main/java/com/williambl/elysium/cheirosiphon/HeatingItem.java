package com.williambl.elysium.cheirosiphon;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import com.williambl.elysium.cheirosiphon.*;

public interface HeatingItem {
    int getMaxHeat();

    int getHeat(int var1, PlayerEntity var2, @Nullable Hand var3, boolean var4);

    default boolean isOverheated(PlayerEntity player) {
        return ((HeatingItemsComponent)player.getComponent(HeatingItemsComponent.KEY)).isOverheated(this);
    }

    default void startHeating(PlayerEntity player) {
        ((HeatingItemsComponent)player.getComponent(HeatingItemsComponent.KEY)).startHeating(this);
    }
}