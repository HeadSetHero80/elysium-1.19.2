package com.williambl.elysium.mixin;

import com.williambl.elysium.fire.ElysiumFireBlock;
import com.williambl.elysium.registry.ElysiumBlocks;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public class BaseFireBlockMixin {
    @Inject(
            method = "getState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/SoulFireBlock;isSoulBase(Lnet/minecraft/block/BlockState;)Z"
            ),
            cancellable = true
    )
    private static void elysium$placeElysiumFire(BlockView world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);

        if (ElysiumFireBlock.canSurviveOnBlock(blockState)) {
            cir.setReturnValue(ElysiumBlocks.ELYSIUM_FIRE.getDefaultState());
        }
    }
}

