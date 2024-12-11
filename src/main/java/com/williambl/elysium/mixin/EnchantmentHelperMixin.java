package com.williambl.elysium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.williambl.elysium.CustomEnchant2;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({EnchantmentHelper.class})
public class EnchantmentHelperMixin {
    @WrapOperation(
            method = {"removeConflicts"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/Enchantment;canCombine(Lnet/minecraft/enchantment/Enchantment;)Z"
            )}
    )
    private static boolean elysium$allowCustomEnchantingRules(Enchantment instance, Enchantment other, Operation<Boolean> original, @Local Enchantment enchantment) {
        if (enchantment instanceof CustomEnchant2) {
            CustomEnchant2 custom = (CustomEnchant2) enchantment;
            return custom.customCanEnchant2(instance, other);
        } else {
            return original.call(instance, other);
        }
    }
}