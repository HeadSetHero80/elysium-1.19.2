package com.williambl.elysium.cheirosiphon;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public interface HeatingItemHeatCallback {
    Event<HeatingItemHeatCallback> EVENT = EventFactory.createArrayBacked(HeatingItemHeatCallback.class, (callbacks) -> (item, lastTickHeat, player, hand, isBeingUsed) -> {
        for(HeatingItemHeatCallback callback : callbacks) {
            OptionalInt res = callback.getHeat(item, lastTickHeat, player, hand, isBeingUsed);
            if (res.isPresent()) {
                return res;
            }
        }

        return OptionalInt.empty();
    });

    OptionalInt getHeat(HeatingItem var1, int var2, PlayerEntity var3, @Nullable Hand var4, boolean var5);
}