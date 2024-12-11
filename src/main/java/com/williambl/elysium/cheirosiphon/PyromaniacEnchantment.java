package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.CustomEnchantment;
import com.williambl.elysium.registry.ElysiumItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PyromaniacEnchantment extends Enchantment implements CustomEnchantment {
    public PyromaniacEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    public boolean customCanEnchant(ItemStack stack) {
        return stack.isOf(ElysiumItems.CHEIROSIPHON);
    }

    public boolean isAcceptableItem(@NotNull ItemStack stack) {
        return this.customCanEnchant(stack);
    }
}