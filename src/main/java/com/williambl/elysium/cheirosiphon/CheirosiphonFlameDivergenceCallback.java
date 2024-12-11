package com.williambl.elysium.cheirosiphon;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface CheirosiphonFlameDivergenceCallback {
    Event<CheirosiphonFlameDivergenceCallback> EVENT = EventFactory.createArrayBacked(CheirosiphonFlameDivergenceCallback.class, (callbacks) -> (user, itemStack, divergence) -> {
        for(CheirosiphonFlameDivergenceCallback callback : callbacks) {
            divergence = callback.modifyDivergence(user, itemStack, divergence);
        }

        return divergence;
    });

    float modifyDivergence(LivingEntity var1, ItemStack var2, float var3);
}