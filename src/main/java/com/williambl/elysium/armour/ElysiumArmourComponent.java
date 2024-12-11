package com.williambl.elysium.armour;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.particles.ArcParticleOption;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ElysiumArmourComponent implements Component, AutoSyncedComponent, ClientTickingComponent {
    private static final int MAX_CHARGE = 10;
    private final LivingEntity entity;
    private int charge;
    public static final ComponentKey<ElysiumArmourComponent> KEY = ComponentRegistry.getOrCreate(Elysium.id("elysium_armour"), ElysiumArmourComponent.class);

    public ElysiumArmourComponent(LivingEntity entity) {
        this.entity = entity;
    }

    public void addCharge(float damageAmount) {
        if (this.entity.getRandom().nextFloat() * 5.0F < damageAmount) {
            ++this.charge;
            KEY.sync(this.entity);
        }

    }

    public int getCharge() {
        return this.charge;
    }

    public int getMaxCharge() {
        return 10;
    }

    public void setChargeNoSync(int charge) {
        this.charge = charge;
    }

    public void decrementCharge() {
        --this.charge;
        KEY.sync(this.entity);
    }

    public void dischargeHurt(Entity entity) {
        if (!((ElysiumArmourHurtDischargeCallback)ElysiumArmourHurtDischargeCallback.EVENT.invoker()).handleHurtDischarge(this, this.entity, entity)) {
            this.dischargeHurtDefault(entity);
        }

        KEY.sync(this.entity);
    }

    public void dischargeHurtDefault(Entity entity) {
        float damage = (float)this.charge * 0.75F;
        float nonPiercingDamage = damage * 0.65F;
        float piercingDamage = damage - nonPiercingDamage;
        entity.damage(DamageSource.GENERIC, nonPiercingDamage);
        entity.damage(DamageSource.GENERIC.setBypassesArmor(), piercingDamage); World var6 = this.entity.getWorld();
        if (var6 instanceof ServerWorld) {
            ServerWorld level = (ServerWorld)var6;
            level.spawnParticles(new ArcParticleOption(entity.getX(), entity.getBodyY(0.5D), entity.getZ()), this.entity.getX(), this.entity.getRandomBodyY(), this.entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound((PlayerEntity)null, this.entity.getBlockPos(), ElysiumSounds.ELECTRODE_ZAP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }

        this.charge = 0;
    }

    public void dischargeVuln(LivingEntity entity) {
        if (!((ElysiumArmourVulnDischargeCallback)ElysiumArmourVulnDischargeCallback.EVENT.invoker()).handleVulnDischarge(this, this.entity, entity)) {
            this.dischargeVulnDefault(entity);
        }

        KEY.sync(this.entity);
    }

    public void dischargeVulnDefault(LivingEntity entity) {
        int length = Math.min(this.charge, 5) * 20;
        int amplifier = this.vulnerabilityLevel() - 1;
        entity.addStatusEffect(new StatusEffectInstance(Elysium.ELYSIUM_VULNERABILITY, length, amplifier));
        World var5 = this.entity.getWorld();
        if (var5 instanceof ServerWorld) {
            ServerWorld level = (ServerWorld)var5;
            level.spawnParticles(new ArcParticleOption(entity.getX(), entity.getBodyY(0.5D), entity.getZ()), this.entity.getX(), this.entity.getRandomBodyY(), this.entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.playSound((PlayerEntity)null, this.entity.getBlockPos(), ElysiumSounds.ELECTRODE_ZAP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        }

        this.charge = 0;
    }

    public void dischargeRandomly() {
        Vec3d target = this.entity.getPos().add(this.entity.getRandom().nextDouble() * 5.0D, this.entity.getRandom().nextDouble() * 5.0D, this.entity.getRandom().nextDouble() * 5.0D);
        this.entity.getWorld().getOtherEntities(this.entity, new Box(target.subtract(-0.5D, -0.5D, -0.5D), target.add(0.5D, 0.5D, 0.5D))).stream().findAny().ifPresentOrElse(this::dischargeHurt, () -> {
            if (!((ElysiumArmourHurtDischargeCallback)ElysiumArmourHurtDischargeCallback.EVENT.invoker()).handleHurtDischarge(this, this.entity, (Entity)null)) {
                World patt4219$temp = this.entity.getWorld();
                if (patt4219$temp instanceof ServerWorld) {
                    ServerWorld level = (ServerWorld)patt4219$temp;
                    level.spawnParticles(new ArcParticleOption(target.x, target.y, target.z), this.entity.getX(), this.entity.getRandomBodyY(), this.entity.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                    level.playSound((PlayerEntity)null, this.entity.getBlockPos(), ElysiumSounds.ELECTRODE_ZAP, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                }
            }

        });
        this.charge = 0;
        KEY.sync(this.entity);
    }

    public boolean shouldDischargeAfterTakingDamage() {
        return this.hasMaxCharge() && this.hasElysiumArmour() && this.entity.getRandom().nextBoolean();
    }

    public boolean hasMaxCharge() {
        return this.charge >= 10;
    }

    public boolean shouldDischargeWhenAttacking() {
        return this.hasElysiumArmour() && this.entity.getRandom().nextBetween(1, 4) <= this.vulnerabilityLevel();
    }

    public int vulnerabilityLevel() {
        byte var10000;
        switch(this.charge) {
            case 0:
                var10000 = 0;
                break;
            case 1:
            case 2:
            case 3:
                var10000 = 1;
                break;
            case 4:
            case 5:
            case 6:
                var10000 = 2;
                break;
            case 7:
            case 8:
            case 9:
                var10000 = 3;
                break;
            default:
                var10000 = 4;
        }

        return var10000;
    }

    public boolean hasElysiumArmour() {
        for(ItemStack slot : this.entity.getArmorItems()) {
            if (!slot.isIn(ElysiumItems.ELYSIUM_ARMOUR_TAG)) {
                return false;
            }
        }

        return true;
    }

    public void readFromNbt(@NotNull NbtCompound tag) {
    }

    public void writeToNbt(@NotNull NbtCompound tag) {
    }

    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        buf.writeByte(this.charge);
    }

    public void applySyncPacket(PacketByteBuf buf) {
        this.charge = buf.readByte();
    }

    public void clientTick() {
        if (this.hasElysiumArmour() && this.entity.age % 10 == 0) {
            Random random = (Random) this.entity.getRandom();
            int particles = (int)((float)this.charge * random.nextFloat());

            for(int i = 0; i < particles; ++i) {
                this.entity.getWorld().addParticle(new ArcParticleOption(this.entity.getParticleX(0.5D), this.entity.getBodyY(random.nextDouble() * 0.8D), this.entity.getParticleZ(0.5D), 0.3F, this.entity.getId()), this.entity.getParticleX(0.5D), this.entity.getRandomBodyY(), this.entity.getParticleZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
        }

    }
}