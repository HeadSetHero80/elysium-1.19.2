package com.williambl.elysium.armour;

import com.williambl.elysium.cheirosiphon.CheirosiphonItem;
import com.williambl.elysium.machine.gravitator.GravitatorBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class GravitatingEnchantment extends Enchantment implements ElysiumArmourHurtDischargeCallback, ElysiumArmourVulnDischargeCallback {
    private final boolean isPushing;

    protected GravitatingEnchantment(boolean isPushing) {
        super(Enchantment.Rarity.RARE, EnchantmentTarget.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
        this.isPushing = isPushing;
        ElysiumArmourHurtDischargeCallback.EVENT.register(this);
        ElysiumArmourVulnDischargeCallback.EVENT.register(this);
    }

    public boolean handleHurtDischarge(ElysiumArmourComponent armour, LivingEntity wearer, @Nullable Entity target) {
        if (EnchantmentHelper.getEquipmentLevel(this, wearer) == 0) {
            return false;
        } else {
            double chargeFactor = (double)armour.getCharge() / (double)armour.getMaxCharge();
            if (target == null) {
                GravitatorBlockEntity.pushEntity(new Vec3d(0.0D, 4.0D * chargeFactor, 0.0D), wearer);
                if (!wearer.getWorld().isClient()) {
                    CheirosiphonItem.ClientboundAirblastFxPacket.sendToTracking(wearer, new Vec3d(0.0D, -0.4D, 0.0D), wearer.getEyePos());
                }
            } else {
                Vec3d pushVec = target.getPos().subtract(wearer.getPos()).normalize().multiply(4.0D * chargeFactor, 2.5D * chargeFactor, 4.0D * chargeFactor).multiply(this.isPushing ? 1.0D : -1.0D);
                GravitatorBlockEntity.pushEntity(pushVec, target);
                if (!wearer.getWorld().isClient()) {
                    CheirosiphonItem.ClientboundAirblastFxPacket.sendToTracking(wearer, pushVec.multiply(0.4D), this.isPushing ? wearer.getEyePos() : wearer.getEyePos().subtract(pushVec));
                }
            }

            armour.setChargeNoSync(0);
            return true;
        }
    }

    public boolean handleVulnDischarge(ElysiumArmourComponent armour, LivingEntity wearer, LivingEntity target) {
        if (EnchantmentHelper.getEquipmentLevel(this, wearer) == 0) {
            return false;
        } else {
            double chargeFactor = (double)armour.getCharge() / (double)armour.getMaxCharge();
            Vec3d pushVec = target.getPos().subtract(wearer.getPos()).normalize().multiply(2.0D * chargeFactor, 1.5D * chargeFactor, 2.0D * chargeFactor).multiply(this.isPushing ? 1.0D : -1.0D).add(0.0D, 0.2D, 0.0D);
            GravitatorBlockEntity.pushEntity(pushVec, target);
            if (!wearer.getWorld().isClient()) {
                CheirosiphonItem.ClientboundAirblastFxPacket.sendToTracking(wearer, pushVec.multiply(0.4D), this.isPushing ? wearer.getEyePos() : wearer.getEyePos().subtract(pushVec));
            }

            armour.dischargeVulnDefault(target);
            return true;
        }
    }
}