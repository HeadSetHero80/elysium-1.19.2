package com.williambl.elysium.armour;

import com.williambl.elysium.client.ElysiumArmourRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.renderers.geo.ArmorRendererProvider;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ElysiumArmourItem extends ArmorItem implements IAnimatable {
    private final AnimationFactory cache = GeckoLibUtil.createFactory(this);

    public ElysiumArmourItem(ArmorMaterial armorMaterial, EquipmentSlot type, Item.Settings properties) {
        super(armorMaterial, type, properties);
    }

    @Override
    public void registerControllers(AnimationData data) {

    }

    @Override
    public AnimationFactory getFactory() {
        return null;
    }
}
