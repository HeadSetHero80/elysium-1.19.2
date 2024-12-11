package com.williambl.elysium.machine.prism;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.ElysiumUtil;
import com.williambl.elysium.client.ElysiumPrismSoundInstance;
import com.williambl.elysium.machine.BeamPowered;
import com.williambl.elysium.machine.ElysiumMachineBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ElysiumPrismBlockEntity extends BlockEntity implements BeamPowered {
    private static final int TICKS_BETWEEN_BEAM_WOBBLES = 10;
    @Nullable
    private BlockPos laserEnd;
    @Nullable
    private BlockPos beaconPos;
    private float prevRenderingBeamWobble = 0.0F;
    private float currRenderingBeamWobble = 0.0F;

    public ElysiumPrismBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ElysiumBlocks.ELYSIUM_PRISM_BLOCK_ENTITY, blockPos, blockState);
    }

    public static void tick(World level, BlockPos pos, BlockState state, ElysiumPrismBlockEntity be) {
        int power = state.get(ElysiumBlocks.ELYSIUM_POWER);
        if (be.getBeamSourcePos() != null) {
            int actualPower = be.getBeamPower(level);
            if (actualPower == 0) {
                be.setBeamSourcePos((BlockPos)null);
            }

            if (actualPower != power) {
                level.setBlockState(pos, state.getStateForNeighborUpdate(Direction.DOWN, level.getBlockState(pos.down()), level, pos, pos.down()));
            }
        }

        if (power > 0 && be.laserEnd != null) {
            level.getNonSpectatingEntities(LivingEntity.class, new Box((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(be.laserEnd.getX() + 1), (double)(be.laserEnd.getY() + 1), (double)(be.laserEnd.getZ() + 1))).forEach((e) -> {
                float damage = (float)((EntityInPrismBeamCallback)EntityInPrismBeamCallback.EVENT.invoker()).entityInBeam(e, power).orElse((double)power);
                e.damage(DamageSource.LIGHTNING_BOLT, damage);
            });
        }

        if ((pos.asLong() + level.getTime()) % 5L == 0L) {
            Direction dir = (Direction)state.get(Properties.FACING);
            BlockPos oldEndPos = be.laserEnd;
            if (power < 1) {
                be.laserEnd = null;
                modifyMachinePower(pos, level, 0, dir, oldEndPos, (BlockPos)null);
            } else {
                BlockPos.Mutable mPos = pos.mutableCopy();

                for(int i = 0; i < 32; ++i) {
                    mPos.move(dir);
                    BlockState checkingState = level.getBlockState(mPos);
                    if (checkingState.isSideSolid(level, mPos, dir.getOpposite(), SideShapeType.CENTER) || checkingState.isSideSolid(level, mPos, dir, SideShapeType.CENTER)) {
                        break;
                    }
                }

                be.laserEnd = mPos.toImmutable();
                ElysiumPrismBlockEntity.ClientboundPrismLaserPacket.sendToTracking(be);
                modifyMachinePower(pos, level, state.get(ElysiumBlocks.ELYSIUM_POWER), dir, oldEndPos, be.laserEnd);
            }
        }
    }

    public static void clientTick(World level, BlockPos pos, BlockState state, ElysiumPrismBlockEntity be) {
        int power = state.get(ElysiumBlocks.ELYSIUM_POWER);
        if (power > 0) {
            ElysiumPrismSoundInstance.play(pos);
            if (level.getTime() % 10L == 0L) {
                be.prevRenderingBeamWobble = be.currRenderingBeamWobble;
                be.currRenderingBeamWobble = level.getRandom().nextFloat() - 0.5F;
            }

            if (level.getTime() % 12L == 0L) {
                Direction facing = (Direction)state.get(Properties.FACING);
                if (level.getBlockState(pos.offset(facing)).isSideSolidFullSquare(level, pos.offset(facing), facing.getOpposite())) {
                    return;
                }

                Vec3i dir = facing.getVector();
                Vec3d offset = ElysiumUtil.getRandomOrthogonal(facing, (Random) level.getRandom()).multiply(0.5D);
                Vec3d particlePos = Vec3d.ofCenter(pos).add(Vec3d.of(dir).multiply(0.5D)).add(offset);
                Vec3d particleVel = Vec3d.of(dir).multiply(0.6D * (double)power * 0.5D);
                level.addParticle(ParticleTypes.FIREWORK, particlePos.x, particlePos.y, particlePos.z, particleVel.x, particleVel.y, particleVel.z);
            }

        }
    }

    public float getRenderingBeamWobble(float partialTicks) {
        float delta = this.world == null ? partialTicks : (partialTicks + (float)((this.world.getTime() - 1L) % 10L)) / 10.0F;
        return MathHelper.lerp(delta, this.prevRenderingBeamWobble, this.currRenderingBeamWobble);
    }

    public void setLaserEnd(@Nullable BlockPos laserEnd) {
        this.laserEnd = laserEnd;
    }

    @Nullable
    public BlockPos getLaserEnd() {
        return this.laserEnd;
    }

    public void resetPower() {
        if (this.world != null && this.laserEnd != null) {
            BlockState state = this.world.getBlockState(this.laserEnd);
            Direction dir = (Direction)this.getCachedState().get(Properties.FACING);
            if (isPowerableMachine(state, dir)) {
                ElysiumMachineBlock machine = (ElysiumMachineBlock)state.getBlock();
                this.world.setBlockState(this.laserEnd, (BlockState)state.with(ElysiumBlocks.ELYSIUM_POWER, machine.getPower(this.world, this.laserEnd, this.world.getBlockState(this.laserEnd))));
            }

        }
    }

    private static void modifyMachinePower(BlockPos prismPos, World level, int power, Direction laserDir, @Nullable BlockPos previousLaserEnd, @Nullable BlockPos newLaserEnd) {
        BlockState prevEndState = previousLaserEnd == null ? null : level.getBlockState(previousLaserEnd);
        BlockState newEndState = newLaserEnd == null ? null : level.getBlockState(newLaserEnd);
        if (prevEndState != null && !previousLaserEnd.equals(newLaserEnd) && isPowerableMachine(prevEndState, laserDir)) {
            ElysiumMachineBlock machine = (ElysiumMachineBlock)prevEndState.getBlock();
            BlockEntity var10 = level.getBlockEntity(previousLaserEnd);
            if (var10 instanceof BeamPowered) {
                BeamPowered beamPowered = (BeamPowered)var10;
                beamPowered.setBeamSourcePos((BlockPos)null);
            }

            level.setBlockState(previousLaserEnd, (BlockState)prevEndState.with(ElysiumBlocks.ELYSIUM_POWER, machine.getPower(level, previousLaserEnd, level.getBlockState(previousLaserEnd))));
        }

        if (newEndState != null && isPowerableMachine(newEndState, laserDir)) {
            BlockEntity var13 = level.getBlockEntity(newLaserEnd);
            if (var13 instanceof BeamPowered) {
                BeamPowered beamPowered = (BeamPowered)var13;
                beamPowered.setBeamSourcePos(prismPos);
            }

            ElysiumMachineBlock machine = (ElysiumMachineBlock)newEndState.getBlock();
            level.setBlockState(newLaserEnd, (BlockState)newEndState.with(ElysiumBlocks.ELYSIUM_POWER, machine.getPower(level, newLaserEnd, newEndState)));
        }

    }

    private static boolean isPowerableMachine(BlockState state, Direction laserDir) {
        Block var3 = state.getBlock();
        if (var3 instanceof ElysiumMachineBlock) {
            ElysiumMachineBlock machine = (ElysiumMachineBlock)var3;
            if (machine.isReceivingSide(state, laserDir.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    public BlockPos getBeamSourcePos() {
        return this.beaconPos;
    }

    public void setBeamSourcePos(@Nullable BlockPos pos) {
        this.beaconPos = pos;
    }

    public boolean canAcceptBeam(Direction beamDir) {
        return ((ElysiumMachineBlock)ElysiumBlocks.ELYSIUM_PRISM).isReceivingSide(this.getCachedState(), beamDir.getOpposite());
    }

    public static final class ClientboundPrismLaserPacket {
        public static final Identifier PACKET_ID = Elysium.id("prism_laser");

        public static void sendToTracking(ElysiumPrismBlockEntity entity) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(entity.getPos()).writeNullable(entity.getLaserEnd(), PacketByteBuf::writeBlockPos);
            ServerPlayNetworking.send((ServerPlayerEntity) PlayerLookup.tracking(entity), PACKET_ID, buf);
        }
    }
}