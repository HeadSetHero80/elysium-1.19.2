package com.williambl.elysium.armour;

import com.williambl.elysium.machine.prism.EntityInPrismBeamCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;

import java.util.OptionalDouble;

public class ReceptiveEnchantment extends Enchantment implements EntityInPrismBeamCallback {
    protected ReceptiveEnchantment() {
        super(Enchantment.Rarity.RARE, EnchantmentTarget.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD});
        EntityInPrismBeamCallback.EVENT.register(this);
    }

    public OptionalDouble entityInBeam(Entity entity, int power) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (EnchantmentHelper.getEquipmentLevel(this, living) > 0) {
                ElysiumArmourComponent component = (ElysiumArmourComponent)living.getComponent(ElysiumArmourComponent.KEY);
                if (!component.shouldDischargeAfterTakingDamage() && (double)component.getCharge() <= (double)component.getMaxCharge() * 4.0D) {
                    component.addCharge(0.5F);
                }

                return OptionalDouble.of(0.0D);
            }
        }

        return OptionalDouble.empty();
    }
}
