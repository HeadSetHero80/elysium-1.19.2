package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.NotNull;

public class GhastlyFireball extends AbstractFireballEntity {
    private int explosionPower = 1;

    public GhastlyFireball(World world, LivingEntity livingEntity, double d, double e, double f, int i) {
        super((EntityType<? extends AbstractFireballEntity>) Elysium.GHASTLY_FIREBALL, livingEntity, d, e, f, world);
        this.explosionPower = i;
    }

    public GhastlyFireball(EntityType<GhastlyFireball> entityEntityType, World world) {
        super(entityEntityType, world);
    }

    protected void onCollision(HitResult result) {
        super.onCollision(result);
        if (!this.getWorld().isClient) {
            this.getWorld().createExplosion((Entity)null, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, false, Explosion.DestructionType.NONE);
            this.discard();
        }

    }

    protected void onEntityHit(EntityHitResult result) {
        super.onEntityHit(result);
        if (!this.getWorld().isClient) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            target.damage(DamageSource.mobProjectile(this, (LivingEntity) owner), 8.0F);
            if (owner instanceof LivingEntity) {
                this.applyDamageEffects((LivingEntity) owner, target);
            }
        }
    }

    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            this.scheduleVelocityUpdate();
            Entity entity = source.getAttacker();
            if (entity != null && this.squaredDistanceTo(entity.getEyePos()) < 9.0D) {
                if (!this.getWorld().isClient) {
                    Vec3d vec3 = entity.getRotationVector().multiply(4.0D);
                    if (entity instanceof ServerPlayerEntity) {
                        ServerPlayerEntity player = (ServerPlayerEntity) entity;
                        Identifier soundId = Registry.SOUND_EVENT.getId(ElysiumSounds.PARRY);
                        if (soundId != null) {
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                    ElysiumSounds.PARRY,
                                    SoundCategory.PLAYERS,
                                    this.getX(),
                                    this.getY(),
                                    this.getZ(),
                                    2.0F,
                                    1.0F,
                                    this.random.nextLong()
                            ));
                        }

                        ((ServerWorld) this.getWorld()).spawnParticles(
                                ParticleTypes.FLASH,
                                this.getX(),
                                this.getY(),
                                this.getZ(),
                                1,
                                0.0D,
                                0.0D,
                                0.0D,
                                0.0D
                        );
                    }

                    this.setVelocity(vec3);
                    this.powerX = vec3.x * 0.1D;
                    this.powerY = vec3.y * 0.1D;
                    this.powerZ = vec3.z * 0.1D;
                    this.setOwner(entity);
                    this.explosionPower = 3;
                }

                return true;
            } else {
                return false;
            }
        }
    }

    @NotNull
    public ItemStack getStack() {
        ItemStack itemStack = this.getItem();
        return itemStack.isEmpty() ? new ItemStack(ElysiumItems.GHASTLY_FIREBALL_ITEM) : itemStack;
    }

    protected boolean isBurning() {
        return false;
    }

    public void writeCustomDataToNbt(NbtCompound compound) {
        super.writeCustomDataToNbt(compound);
        compound.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    public void readCustomDataFromNbt(NbtCompound compound) {
        super.readCustomDataFromNbt(compound);
        if (compound.contains("ExplosionPower", 99)) {
            this.explosionPower = compound.getByte("ExplosionPower");
        }

    }

    public boolean shouldSave() {
        return false;
    }
}