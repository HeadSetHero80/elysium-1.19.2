package com.williambl.elysium.armour;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;

public class ElysiumArmorMaterial implements ArmorMaterial {
    private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
    private static final int[] DEFENSE_PER_SLOT = new int[]{3, 6, 8, 3};

    public int getDurability(EquipmentSlot slot) {
        return HEALTH_PER_SLOT[slot.ordinal()] * 33;
    }

    public int getProtectionAmount(EquipmentSlot slot) {
        return DEFENSE_PER_SLOT[slot.ordinal()];
    }

    public int getEnchantability() {
        return 5;
    }

    @NotNull
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_IRON;
    }

    @NotNull
    public Ingredient getRepairIngredient() {
        return Ingredient.ofItems(new ItemConvertible[]{Items.DIAMOND});
    }

    @NotNull
    public String getName() {
        return "elysium";
    }

    public float getToughness() {
        return 2.0F;
    }

    public float getKnockbackResistance() {
        return 0.0F;
    }
}
