package com.williambl.elysium.cheirosiphon;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface CheirosiphonFlameSpawningCallback {
    Event<CheirosiphonFlameSpawningCallback> EVENT = EventFactory.createArrayBacked(CheirosiphonFlameSpawningCallback.class, (callbacks) -> (user, stack, flame) -> {
        for(CheirosiphonFlameSpawningCallback callback : callbacks) {
            callback.acceptFlame(user, stack, flame);
        }

    });

    void acceptFlame(LivingEntity var1, ItemStack var2, CheirosiphonFlame var3);
}