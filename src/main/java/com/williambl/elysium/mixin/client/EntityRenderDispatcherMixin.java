package com.williambl.elysium.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.williambl.elysium.client.ElysiumFlameRendering;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderDispatcher.class})
public class EntityRenderDispatcherMixin {
    @Inject(
            method = {"renderFire"},
            at = {@At("HEAD")}
    )
    private void elysium$checkShouldRenderElysiumFlame(MatrixStack matrixStack, VertexConsumerProvider buffer, Entity entity, CallbackInfo ci, @Share("shouldRenderElysium") LocalBooleanRef shouldRenderElysium) {
        shouldRenderElysium.set(ElysiumFlameRendering.shouldRenderElysiumFire(entity));
    }

    @ModifyReceiver(
            method = {"renderFire"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/SpriteIdentifier;getSprite()Lnet/minecraft/client/texture/Sprite;",
                    ordinal = 0
            )}
    )
    private SpriteIdentifier elysium$modifyElysiumFlameTexture0(SpriteIdentifier originalMaterial, @Share("shouldRenderElysium") LocalBooleanRef shouldRenderElysium) {
        return shouldRenderElysium.get() ? ElysiumFlameRendering.FIRE_0 : originalMaterial;
    }

    @ModifyReceiver(
            method = {"renderFire"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/SpriteIdentifier;getSprite()Lnet/minecraft/client/texture/Sprite;",
                    ordinal = 1
            )}
    )
    private SpriteIdentifier elysium$modifyElysiumFlameTexture1(SpriteIdentifier originalMaterial, @Share("shouldRenderElysium") LocalBooleanRef shouldRenderElysium) {
        return shouldRenderElysium.get() ? ElysiumFlameRendering.FIRE_1 : originalMaterial;
    }
}