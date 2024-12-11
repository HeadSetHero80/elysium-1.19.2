package com.williambl.elysium.mixin;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.registry.ElysiumItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected ItemStack activeItemStack;

    public LivingEntityMixin(EntityType<?> entityType, World level) {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    public abstract StatusEffectInstance getStatusEffect(StatusEffect var1);

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect var1);

    @Shadow
    public abstract boolean removeStatusEffect(StatusEffect var1);

    @Inject(
            method = {"tick"},
            at = {@At("HEAD")}
    )
    private void elysium$cancelVulnerability(CallbackInfo ci) {
        if (this.hasStatusEffect(Elysium.ELYSIUM_VULNERABILITY) && this.isInsideWaterOrBubbleColumn()) {
            this.removeStatusEffect(Elysium.ELYSIUM_VULNERABILITY);
        }

    }

    @Inject(
            method = {"swingHand*"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void elysium$dontSwingArmForCheirosiphon(float partialTick, CallbackInfoReturnable<Float> cir) {
        if (this.activeItemStack.isOf(ElysiumItems.CHEIROSIPHON)) {
            cir.setReturnValue(0.0F);
        }

    }
}