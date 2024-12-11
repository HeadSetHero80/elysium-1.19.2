package com.williambl.elysium.mixin;

import com.williambl.elysium.registry.ElysiumItems;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerInventory.class)
public class InventoryMixin {

    @Shadow @Final
    public DefaultedList<ItemStack> armor;

    @Shadow @Final
    public PlayerEntity player;

    @Inject(
            method = "damageArmor",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void elysium$turnExperimentalElysiumIntoIron(DamageSource source, float amount, int[] slots, CallbackInfo ci, int[] var4, int var5, int var6, int i, ItemStack itemStack) {
        if (itemStack.isIn(ElysiumItems.EXPERIMENTAL_ELYSIUM_ARMOUR_TAG) && this.player.getRandom().nextInt(4) == 0) {
            EquipmentSlot slot = EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i);
            ItemStack newArmorItem = new ItemStack(getIronArmorForSlot(slot));
            this.armor.set(i, newArmorItem);
            this.player.sendEquipmentBreakStatus(slot);
        }
    }

    private Item getIronArmorForSlot(EquipmentSlot slot) {
        switch (slot) {
            case FEET: return Items.IRON_BOOTS;
            case LEGS: return Items.IRON_LEGGINGS;
            case CHEST: return Items.IRON_CHESTPLATE;
            case HEAD: return Items.IRON_HELMET;
            default: return Items.AIR;
        }
    }
}