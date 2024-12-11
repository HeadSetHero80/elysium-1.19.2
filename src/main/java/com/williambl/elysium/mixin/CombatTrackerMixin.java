package com.williambl.elysium.mixin;

import com.williambl.elysium.armour.ElysiumArmourComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DamageTracker.class})
public class CombatTrackerMixin {
    @Shadow
    @Final
    private LivingEntity entity;

    @Inject(
            method = {"onDamage"},
            at = {@At("HEAD")}
    )
    private void elysium$chargeElysiumArmour(DamageSource damageSource, float originalHealth, float damage, CallbackInfo ci) {
        ElysiumArmourComponent.KEY.maybeGet(this.entity).ifPresent((comp) -> {
            if (comp.hasElysiumArmour()) {
                if (damageSource.getSource() instanceof TridentEntity) {
                    if (comp.hasMaxCharge()) {
                        comp.dischargeRandomly();
                    } else {
                        comp.decrementCharge();
                    }
                } else {
                    comp.addCharge(damage);
                    if (comp.shouldDischargeAfterTakingDamage() && damageSource.getAttacker() != null) {
                        comp.dischargeHurt(damageSource.getAttacker());
                    }
                }
            }

        });
    }
}