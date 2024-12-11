package com.williambl.elysium.client;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.armour.ElysiumArmourItem;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

public class ElysiumArmourRenderer extends GeoArmorRenderer<ElysiumArmourItem> {
    private static final Identifier MODEL = Elysium.id("geo/elysium_armour.geo.json");
    private static final Identifier TEXTURE = Elysium.id("textures/model/elysium_armour.png");
    private static final Identifier ANIM = Elysium.id("animations/elysium_armour.animation.json");
    private static final String chestplateBody = "chestplateBody";
    private static final String leggingsBody = "leggingsBody";

    public ElysiumArmourRenderer() {
        super(new DefaultedItemGeoModel<ElysiumArmourItem>(Elysium.id("armor/elysium_armour")) {
            public Identifier getAnimationResource(ElysiumArmourItem animatable) {
                return ElysiumArmourRenderer.ANIM;
            }

            public Identifier getModelResource(ElysiumArmourItem object) {
                return ElysiumArmourRenderer.MODEL;
            }

            public Identifier getTextureResource(ElysiumArmourItem object) {
                return ElysiumArmourRenderer.TEXTURE;
            }
        });
    }

    protected void applyBoneVisibilityBySlot(EquipmentSlot slot) {
        super.applyBoneVisibilityBySlot(slot);
        if (slot == EquipmentSlot.LEGS) {
            this.setBoneVisible(this.getBodyBone(), true);
            this.setBoneVisible((GeoBone)this.model.getBone("chestplateBody").orElse((Object)null), false);
            this.setBoneVisible((GeoBone)this.model.getBone("leggingsBody").orElse((Object)null), true);
        } else if (slot == EquipmentSlot.CHEST) {
            this.setBoneVisible(this.getBodyBone(), true);
            this.setBoneVisible((GeoBone)this.model.getBone("chestplateBody").orElse((Object)null), true);
            this.setBoneVisible((GeoBone)this.model.getBone("leggingsBody").orElse((Object)null), false);
        }

    }

    public void prepForRender(@Nullable Entity entity, ItemStack stack, @Nullable EquipmentSlot slot, @Nullable BipedEntityModel<?> baseModel) {
        super.prepForRender(entity, stack, slot, baseModel);
    }
}