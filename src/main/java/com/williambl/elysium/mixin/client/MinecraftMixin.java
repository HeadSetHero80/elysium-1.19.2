package com.williambl.elysium.mixin.client;

import com.williambl.elysium.cheirosiphon.CheirosiphonItem;
import com.williambl.elysium.registry.ElysiumItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MinecraftClient.class})
public class MinecraftMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Inject(
            method = {"doAttack"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void elysium$useCheirosiphonLeftClickAction(CallbackInfoReturnable<Boolean> cir) {
        if (this.player != null && this.player.getMainHandStack().isOf(ElysiumItems.CHEIROSIPHON)) {
            ClientPlayNetworking.send(CheirosiphonItem.ServerboundAirblastPacket.PACKET_ID, PacketByteBufs.create());
            cir.setReturnValue(true);
        }

    }
}