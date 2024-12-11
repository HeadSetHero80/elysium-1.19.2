package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.machine.gravitator.GravitatorBlockEntity;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class CheirosiphonItem extends Item implements HeatingItem {
    private static final int MAX_USE_TICKS = 100;

    public CheirosiphonItem(Item.Settings properties) {
        super(properties);
    }

    @NotNull
    public TypedActionResult<ItemStack> use(@NotNull World level, PlayerEntity user, @NotNull Hand hand) {
        if ((user.isCreative() || user.getInventory().contains(Items.FIRE_CHARGE.getDefaultStack())) && !user.isTouchingWater() && !this.isOverheated(user)) {
            ItemStack stack = user.getStackInHand(hand);
            user.setCurrentHand(hand);
            this.startHeating(user);
            Inventories.remove(user.getInventory(), (s) -> s.isOf(Items.FIRE_CHARGE), 1, false);
            return TypedActionResult.success(stack, level.isClient());
        } else {
            return TypedActionResult.fail(user.getStackInHand(hand));
        }
    }

    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    public int getMaxUseTime(@NotNull ItemStack stack) {
        return 999999990;
    }

    @NotNull
    public UseAction getUseAction(@NotNull ItemStack stack) {
        return UseAction.NONE;
    }

    public void usageTick(@NotNull World level, @NotNull LivingEntity user, @NotNull ItemStack itemStack, int i) {
        if (i % 2 == 0) {
            user.playSoundIfNotSilent(ElysiumSounds.CHEIROSIPHON_LOOP);
        }

        if (user instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity) user;
            if (this.isOverheated(p)) {
                p.playSoundIfNotSilent(ElysiumSounds.CHEIROSIPHON_DEACTIVATE);
                p.clearActiveItem();

                DamageSource damageSource = p.getActiveHand() == Hand.MAIN_HAND
                        ? DamageSource.HOT_FLOOR
                        : DamageSource.IN_FIRE;

                p.damage(damageSource, 1.0F);
                p.playSoundIfNotSilent(SoundEvents.ENTITY_GENERIC_BURN);
                p.setOnFireFor(6);
                return;
            }
        }

        if (i % 120 == 0 && user instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity)user;
            if (!p.isCreative() && !p.getInventory().contains(Items.FIRE_CHARGE.getDefaultStack()) || user.isTouchingWater()) {
                p.playSoundIfNotSilent(ElysiumSounds.CHEIROSIPHON_DEACTIVATE);
                p.clearActiveItem();
                return;
            }

            Inventories.remove(p.getInventory(), (s) -> s.isOf(Items.FIRE_CHARGE), 1, false);
        }

        if (!level.isClient()) {
            CheirosiphonFlame flame = new CheirosiphonFlame(Elysium.CHEIROSIPHON_FLAME, user, level);
            float divergence = ((CheirosiphonFlameDivergenceCallback)CheirosiphonFlameDivergenceCallback.EVENT.invoker()).modifyDivergence(user, itemStack, 30.0F);
            float speed = ((CheirosiphonFlameSpeedCallback)CheirosiphonFlameSpeedCallback.EVENT.invoker()).modifySpeed(user, itemStack, 1.0F);
            flame.setVelocity(user, divergence, speed);
            ((CheirosiphonFlameSpawningCallback)CheirosiphonFlameSpawningCallback.EVENT.invoker()).acceptFlame(user, itemStack, flame);
            level.spawnEntity(flame);
        }

    }

    public void airBlast(World level, PlayerEntity user) {
        level.getOtherEntities(user, user.getBoundingBox().stretch(user.getRotationVec(1.0F).multiply(4.0D))).forEach((e) -> {
            e.extinguish();
            DamageSource damageSource = DamageSource.explosion(user);
            e.damage(damageSource, 0.1F);
            GravitatorBlockEntity.pushEntity(user.getRotationVec(1.0F).multiply(3.0D, 1.5D, 3.0D), e);
        });

        user.getItemCooldownManager().set(this, 20);
        CheirosiphonItem.ClientboundAirblastFxPacket.sendToTracking(user);
    }

    public int getMaxHeat() {
        return 100;
    }

    public int getHeat(int lastTickHeat, PlayerEntity player, @Nullable Hand hand, boolean isBeingUsed) {
        if (isBeingUsed) {
            return lastTickHeat + (hand == Hand.MAIN_HAND ? 1 : 2);
        } else {
            return hand == Hand.MAIN_HAND ? lastTickHeat - (int)(player.getWorld().getTime() % 2L) : lastTickHeat - (player.getWorld().getTime() % 3L == 0L ? 0 : 1);
        }
    }

    public static final class ClientboundAirblastFxPacket {
        public static final Identifier PACKET_ID = Elysium.id("cheirosiphon_airblast_fx");

        public static void sendToTracking(Entity entity, Vec3d direction, Vec3d spawnPosition) {
            ServerPlayNetworking.send((Collection) Util.make(new ArrayList(PlayerLookup.tracking(entity)), (c) -> {
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity s = (ServerPlayerEntity) entity;
                    c.add(s);
                }

            }), PACKET_ID, create(direction, spawnPosition));
        }

        public static void sendToTracking(Entity entity) {
            Vec3d direction = entity.getRotationVec(1.0F).multiply(0.45D);
            Vec3d spawnPosition = entity.getCameraPosVec(1.0F).add(entity.getRotationVec(1.0F));
            sendToTracking(entity, direction, spawnPosition);
        }

        public static PacketByteBuf create(Vec3d direction, Vec3d spawnPosition) {
            return new PacketByteBuf(PacketByteBufs.create().writeDouble(direction.getX()).writeDouble(direction.getY()).writeDouble(direction.getZ()).writeDouble(spawnPosition.getX()).writeDouble(spawnPosition.getY()).writeDouble(spawnPosition.getZ()));
        }
    }

    public static final class ServerboundAirblastPacket {
        public static final Identifier PACKET_ID = Elysium.id("cheirosiphon_airblast");

        public static void init() {
            ServerPlayNetworking.registerGlobalReceiver(PACKET_ID, (server, player, handler, buf, responseSender) -> server.execute(() -> {
                if (player.getMainHandStack().isOf(ElysiumItems.CHEIROSIPHON) && !player.getItemCooldownManager().isCoolingDown(ElysiumItems.CHEIROSIPHON) && !((HeatingItem)ElysiumItems.CHEIROSIPHON).isOverheated(player) && !((CheirosiphonAirblastCallback)CheirosiphonAirblastCallback.EVENT.invoker()).handleAirblast(player, player.getMainHandStack())) {
                    ((CheirosiphonItem) ElysiumItems.CHEIROSIPHON).airBlast(player.getWorld(), player);
                }

            }));
        }
    }
}