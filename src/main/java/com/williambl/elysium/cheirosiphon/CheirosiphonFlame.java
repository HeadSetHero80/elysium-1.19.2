package com.williambl.elysium.cheirosiphon;

import com.williambl.elysium.Elysium;
import net.minecraft.data.server.AdvancementProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class CheirosiphonFlame extends ThrownItemEntity {
    private static final TrackedData<Boolean> CONCENTRATED_ID = DataTracker.registerData(CheirosiphonFlame.class, TrackedDataHandlerRegistry.BOOLEAN);

    public CheirosiphonFlame(EntityType<CheirosiphonFlame> entityEntityType, World world){
        super((EntityType<? extends ThrownItemEntity>) entityEntityType, world);
        this.setPosition(this.getX(), this.getY() - (double)(this.getHeight() / 2.0F), this.getZ());
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(CONCENTRATED_ID, false);
    }

    public void setConcentrated(boolean concentrated) {
        this.dataTracker.set(CONCENTRATED_ID, concentrated);
    }

    private boolean isConcentrated() {
        return this.dataTracker.get(CONCENTRATED_ID);
    }

    @NotNull
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;
    }

    protected void onCollision(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            this.onBlockHit(blockHitResult);
        }
    }

    protected void onBlockHit(@NotNull BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        this.discard();
    }

    public boolean doesRenderOnFire() {
        return false;
    }

    public boolean isOnFire() {
        return true;
    }

    public boolean isFireImmune() {
        return true;
    }

    public boolean hasNoGravity() {
        return true;
    }

    public void tick() {
        super.tick();
        if (this.age > (this.isConcentrated() ? 20 : 30)) {
            this.discard();
        }

        if (!this.getWorld().isClient()) {
            this.getWorld()
                    .getOtherEntities(this, this.getBoundingBox().expand(0.25D), (e) -> !this.isOwner(e))
                    .stream()
                    .filter(LivingEntity.class::isInstance)
                    .forEach((p) -> {
                        p.damage(DamageSource.GENERIC, Math.max(1.5F, 6.0F - (float) this.age * 0.15F));
                        if (!this.isWet()) {
                            p.setOnFireFor(2);
                        }
                    });
        } else {
            double concFactor = this.isConcentrated() ? 0.2D : 1.0D;
            Vec3d particleSpeed = this.getVelocity()
                    .multiply(this.random.nextDouble() * concFactor)
                    .add(
                            this.random.nextDouble() * concFactor * 0.1D,
                            this.random.nextDouble() * concFactor * 0.1D,
                            this.random.nextDouble() * concFactor * 0.1D
                    );
            this.getWorld()
                    .addParticle(
                            Elysium.ELYSIUM_FLAME_PARTICLE,
                            this.getParticleX(this.getWidth()),
                            this.getRandomBodyY(),
                            this.getParticleZ((double) this.getWidth()),
                            particleSpeed.getX(),
                            particleSpeed.getY(),
                            particleSpeed.getZ()
                    );

            if (this.isInsideWaterOrBubbleColumn()) {
                this.getWorld()
                        .addParticle(
                                ParticleTypes.BUBBLE,
                                this.getParticleX(this.getWidth()),
                                this.getRandomBodyY(),
                                this.getParticleZ((double) this.getWidth()),
                                this.getVelocity().getX() * this.random.nextDouble(),
                                this.getVelocity().getY() * this.random.nextDouble(),
                                this.getVelocity().getZ() * this.random.nextDouble()
                        );
            }
        }

    }

    public void setVelocity(Entity user, float divergence, float speed) {
        float xRot = user.getPitch();
        float yRot = user.getYaw() + (this.random.nextFloat() - 0.5F) * divergence;
        Vec3d direction = this.getRotationVector(xRot, yRot).normalize();
        this.setVelocity(direction.multiply((double)speed));
        this.setPitch(xRot);
        this.setYaw(yRot);
    }
}