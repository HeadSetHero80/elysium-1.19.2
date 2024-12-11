package com.williambl.elysium.mixin;

import com.williambl.elysium.machine.BeamPowered;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/DyeColor;getColorComponents()[F"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void elysium$powerBlocks(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci) {
        BlockEntity var15 = world.getBlockEntity(pos);
        if (var15 instanceof BeamPowered) {
            BeamPowered beamPoweredBE = (BeamPowered) var15;
            if (beamPoweredBE.canAcceptBeam(Direction.UP)) {
                beamPoweredBE.setBeamSourcePos(pos);
            }
        }
    }
}