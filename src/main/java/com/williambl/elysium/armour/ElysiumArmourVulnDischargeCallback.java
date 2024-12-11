package com.williambl.elysium.armour;

import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.LivingEntity;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ElysiumArmourVulnDischargeCallback {
    Event<ElysiumArmourVulnDischargeCallback> EVENT = EventFactory.createArrayBacked(
            ElysiumArmourVulnDischargeCallback.class,
            (armour, wearer, target) -> false,
            (callbacks) -> (armour, wearer, target) -> {
                for (ElysiumArmourVulnDischargeCallback callback : callbacks) {
                    if (callback.handleVulnDischarge(armour, wearer, target)) {
                        return true;
                    }
                }
                return false;
            }
    );

    boolean handleVulnDischarge(ElysiumArmourComponent armour, LivingEntity wearer, LivingEntity target);
}


