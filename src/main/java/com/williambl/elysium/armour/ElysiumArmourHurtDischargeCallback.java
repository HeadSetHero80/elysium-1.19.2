package com.williambl.elysium.armour;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface ElysiumArmourHurtDischargeCallback {
    Event<ElysiumArmourHurtDischargeCallback> EVENT = EventFactory.createArrayBacked(
            ElysiumArmourHurtDischargeCallback.class,
            (armour, wearer, target) -> false,
            (listeners) -> (armour, wearer, target) -> {
                for (ElysiumArmourHurtDischargeCallback callback : listeners) {
                    if (callback.handleHurtDischarge(armour, wearer, target)) {
                        return true;
                    }
                }
                return false;
            }
    );

    // Callback method
    boolean handleHurtDischarge(ElysiumArmourComponent armour, LivingEntity wearer, @Nullable net.minecraft.entity.Entity target);
}
