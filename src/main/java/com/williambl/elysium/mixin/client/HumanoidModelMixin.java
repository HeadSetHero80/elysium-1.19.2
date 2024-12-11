package com.williambl.elysium.mixin.client;

import com.williambl.elysium.registry.ElysiumItems;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BipedEntityModel.class})
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart head;
    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(
            method = {"positionLeftArm"},
            at = {@At("TAIL")}
    )
    private void elysium$poseLeftArmForCheirosiphon(T livingEntity, CallbackInfo ci) {
        if (livingEntity.getMainHandStack().isOf(ElysiumItems.CHEIROSIPHON)) {
            animateHold(this.rightArm, this.leftArm, this.head, livingEntity.getMainArm() == Arm.RIGHT);
        } else if (livingEntity.getOffHandStack().isOf(ElysiumItems.CHEIROSIPHON)) {
            animateOffhandHold(this.rightArm, this.leftArm, this.head, livingEntity.getMainArm() == Arm.RIGHT);
        }

    }

    @Inject(
            method = {"positionRightArm"},
            at = {@At("TAIL")}
    )
    private void elysium$poseRightArmForCheirosiphon(T livingEntity, CallbackInfo ci) {
        if (livingEntity.getMainHandStack().isOf(ElysiumItems.CHEIROSIPHON)) {
            animateHold(this.rightArm, this.leftArm, this.head, livingEntity.getMainArm() == Arm.RIGHT);
        } else if (livingEntity.getOffHandStack().isOf(ElysiumItems.CHEIROSIPHON)) {
            animateOffhandHold(this.rightArm, this.leftArm, this.head, livingEntity.getMainArm() == Arm.RIGHT);
        }

    }

    @Unique
    private static void animateHold(ModelPart rightArm, ModelPart leftArm, ModelPart head, boolean rightHanded) {
        ModelPart mainHand = rightHanded ? rightArm : leftArm;
        ModelPart offHand = rightHanded ? leftArm : rightArm;
        mainHand.yaw = (rightHanded ? -0.1F : 0.1F) + head.yaw;
        offHand.yaw = (rightHanded ? 0.7F : -0.7F) + head.yaw;
        mainHand.pitch = (-(float)Math.PI / 2F) + head.pitch + 0.1F;
        offHand.pitch = -1.5F + head.pitch;
    }

    @Unique
    private static void animateOffhandHold(ModelPart rightArm, ModelPart leftArm, ModelPart head, boolean rightHanded) {
        ModelPart offHand = rightHanded ? leftArm : rightArm;
        offHand.yaw = (rightHanded ? -0.1F : 0.1F) + head.yaw;
        offHand.pitch = (-(float)Math.PI / 2F) + head.pitch + 0.1F;
    }
}