package com.williambl.elysium.client;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.cheirosiphon.GhastlyFireball;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.screen.PlayerScreenHandler;

public final class ElysiumFlameRendering {
    public static final SpriteIdentifier FIRE_0 = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Elysium.id("block/elysium_fire_0"));
    public static final SpriteIdentifier FIRE_1 = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Elysium.id("block/elysium_fire_1"));

    public static boolean shouldRenderElysiumFire(Entity entity) {
        return entity instanceof GhastlyFireball;
    }
}