package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.CustomEnchantment;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import com.williambl.elysium.cheirosiphon.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class GhastlyEnchantment extends Enchantment implements CustomEnchantment, CheirosiphonAirblastCallback {
    public GhastlyEnchantment() {
        super(Rarity.RARE, EnchantmentTarget.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        CheirosiphonAirblastCallback.EVENT.register(this);
    }

    public boolean isAcceptableItem(@NotNull ItemStack stack) {
        return this.customCanEnchant(stack);
    }

    public boolean customCanEnchant(ItemStack stack) {
        return stack.isOf(ElysiumItems.CHEIROSIPHON);
    }

    public boolean handleAirblast(LivingEntity user, ItemStack cheirosiphon) {
        if (EnchantmentHelper.getLevel(this, cheirosiphon) > 0) {
            Vec3d direction = user.getRotationVec(1.0F).normalize();
            GhastlyFireball ball = new GhastlyFireball(user.getWorld(), user, direction.getX(), direction.getY(), direction.getZ(), 1);
            ball.setPosition(user.getX() + direction.getX(), user.getEyeY() + direction.getY(), user.getZ() + direction.getZ());
            ball.powerX = direction.getX() * 0.2D;
            ball.powerY = direction.getY() * 0.2D;
            ball.powerZ = direction.getZ() * 0.2D;
            user.getWorld().spawnEntity(ball);
            if (user instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity)user;
                player.getItemCooldownManager().set(cheirosiphon.getItem(), 30);
            }

            user.getWorld().playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), ElysiumSounds.CHEIROSIPHON_GHASTLY_BLAST, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            return true;
        } else {
            return false;
        }
    }
}