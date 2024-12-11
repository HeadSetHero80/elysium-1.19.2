package com.williambl.elysium.machine.prism;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.OptionalDouble;

@FunctionalInterface
public interface EntityInPrismBeamCallback {
    Event<EntityInPrismBeamCallback> EVENT = EventFactory.createArrayBacked(EntityInPrismBeamCallback.class, (callbacks) -> (entity, power) -> {
        for(EntityInPrismBeamCallback callback : callbacks) {
            OptionalDouble res = callback.entityInBeam(entity, power);
            if (res.isPresent()) {
                return res;
            }
        }

        return OptionalDouble.empty();
    });

    OptionalDouble entityInBeam(Entity var1, int var2);
}