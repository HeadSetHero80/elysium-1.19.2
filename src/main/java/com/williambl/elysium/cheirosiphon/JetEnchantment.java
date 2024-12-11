package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.CustomEnchantment;
import com.williambl.elysium.registry.ElysiumItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class JetEnchantment extends Enchantment implements CustomEnchantment, CheirosiphonFlameDivergenceCallback, CheirosiphonFlameSpeedCallback, CheirosiphonFlameSpawningCallback, HeatingItemHeatCallback {
    public JetEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot.MAINHAND[]{EquipmentSlot.MAINHAND});
        CheirosiphonFlameDivergenceCallback.EVENT.register(this);
        CheirosiphonFlameSpeedCallback.EVENT.register(this);
        CheirosiphonFlameSpawningCallback.EVENT.register(this);
        HeatingItemHeatCallback.EVENT.register(this);
    }

    public boolean customCanEnchant(ItemStack stack) {
        return stack.isOf(ElysiumItems.CHEIROSIPHON);
    }

    public boolean isAcceptableItem(@NotNull ItemStack stack) {
        return this.customCanEnchant(stack);
    }

    public float modifyDivergence(LivingEntity user, ItemStack stack, float divergence) {
        return EnchantmentHelper.getLevel(this, stack) > 0 ? divergence * 0.1F : divergence;
    }

    public float modifySpeed(LivingEntity user, ItemStack stack, float speed) {
        return EnchantmentHelper.getLevel(this, stack) > 0 ? speed * 2.0F : speed;
    }

    public void acceptFlame(LivingEntity user, ItemStack stack, CheirosiphonFlame flame) {
        if (EnchantmentHelper.getLevel(this, stack) > 0) {
            flame.setConcentrated(true);
        }

    }

    public OptionalInt getHeat(HeatingItem item, int lastTickHeat, PlayerEntity player, @Nullable Hand hand, boolean isBeingUsed) {
        if (hand == null) {
            return OptionalInt.empty();
        } else {
            ItemStack stack = player.getStackInHand(hand);
            if (EnchantmentHelper.getLevel(this, stack) > 0) {
                if (isBeingUsed) {
                    return OptionalInt.of(lastTickHeat + (hand == Hand.MAIN_HAND ? 2 : 3));
                } else {
                    return hand == Hand.MAIN_HAND ? OptionalInt.of(lastTickHeat - (player.getWorld().getTime() % 3L == 0L ? 1 : 0)) : OptionalInt.of(lastTickHeat - (player.getWorld().getTime() % 3L == 0L ? 0 : 1));
                }
            } else {
                return OptionalInt.empty();
            }
        }
    }
}