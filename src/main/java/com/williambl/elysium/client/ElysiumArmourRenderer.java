package com.williambl.elysium.client;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.armour.ElysiumArmourItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper; // Correct import for MathHelper
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoBone; // Use GeoBone instead of IBone
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class ElysiumArmourRenderer extends GeoArmorRenderer<ElysiumArmourItem> {
    private static final Identifier MODEL = Elysium.id("geo/elysium_armour.geo.json");
    private static final Identifier TEXTURE = Elysium.id("textures/model/elysium_armour.png");
    private static final Identifier ANIM = Elysium.id("animations/elysium_armour.animation.json");
    private static final String chestplateBody = "chestplateBody";
    private static final String leggingsBody = "leggingsBody";
    private static final String bodyBone = "body"; // Replace with the correct bone name from your model

    public ElysiumArmourRenderer() {
        super(new AnimatedGeoModel<>() {
            @Override
            public Identifier getModelResource(ElysiumArmourItem object) {
                return MODEL;
            }

            @Override
            public Identifier getTextureResource(ElysiumArmourItem object) {
                return TEXTURE;
            }

            @Override
            public Identifier getAnimationResource(ElysiumArmourItem animatable) {
                return ANIM;
            }
        });
    }

    // Custom method to handle bone visibility based on the equipment slot
    protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
        // Reset all bones to their default visibility
        this.resetBoneVisibility();

        // Custom logic for bone visibility
        if (slot == EquipmentSlot.LEGS) {
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(bodyBone), true);
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(chestplateBody), false);
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(leggingsBody), true);
        } else if (slot == EquipmentSlot.CHEST) {
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(bodyBone), true);
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(chestplateBody), true);
            this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(leggingsBody), false);
        }
    }

    // Helper method to reset bone visibility
    protected void resetBoneVisibility() {
        // Hide specific bones by name
        this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(bodyBone), false);
        this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(chestplateBody), false);
        this.setBoneVisible((GeoBone) this.getGeoModelProvider().getBone(leggingsBody), false);
    }

    // Helper method to set bone visibility
    protected void setBoneVisible(GeoBone bone, boolean visible) {
        if (bone != null) {
            bone.setHidden(!visible);
        }
    }

    // Custom method to prepare for rendering
    public void prepForRender(@Nullable Entity entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable BipedEntityModel<?> baseModel) {
        // Apply custom bone visibility logic
        if (slot != null) {
            this.applyBoneVisibilityBySlot(slot);
        }
    }
}