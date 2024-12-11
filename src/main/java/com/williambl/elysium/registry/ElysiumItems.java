package com.williambl.elysium.registry;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.armour.ElysiumArmorMaterial;
import com.williambl.elysium.armour.ElysiumArmourComponent;
import com.williambl.elysium.armour.ElysiumArmourItem;
import com.williambl.elysium.cheirosiphon.CheirosiphonItem;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public interface ElysiumItems {
    static final ArrayList<ItemStack> ELYSIUM_ITEMS = new ArrayList<>();
    static final ItemGroup ELYSIUM_GROUP = FabricItemGroupBuilder.build(
            new Identifier("elysium", "itemGroup.elysium.elysium"),
            () -> new ItemStack(ElysiumItems.ELYSIUM_INGOT)
    );
    static final Map<Item, Identifier> ITEMS = new LinkedHashMap<>();
    static final ArmorMaterial ELYSIUM_MATERIAL = new ElysiumArmorMaterial();
    static final TagKey<Item> ELYSIUM_ARMOUR_TAG = TagKey.of(Registry.ITEM_KEY, Elysium.id("elysium_armour"));
    static final TagKey<Item> EXPERIMENTAL_ELYSIUM_ARMOUR_TAG = TagKey.of(Registry.ITEM_KEY, Elysium.id("experimental_elysium_armour"));

    static final Item ELYSIUM_INGOT = createItem("elysium_ingot", new Item(new FabricItemSettings()));
    static final Item CHEIROSIPHON = createItem("cheirosiphon", new CheirosiphonItem((new Item.Settings()).maxCount(1)), ElysiumEnchantments.JET_ENCHANTMENT, ElysiumEnchantments.GHASTLY_ENCHANTMENT);
    static final Item GHASTLY_FIREBALL_ITEM = createItemNoRegister("ghastly_fireball", new Item((new Item.Settings()).maxCount(16)));
    static final Item ELYSIUM_HELMET = createItem("elysium_helmet", new ElysiumArmourItem(ELYSIUM_MATERIAL, EquipmentSlot.HEAD, new Item.Settings()));
    static final Item ELYSIUM_CHESTPLATE = createItem("elysium_chestplate", new ElysiumArmourItem(ELYSIUM_MATERIAL, EquipmentSlot.CHEST, new Item.Settings()));
    static final Item ELYSIUM_LEGGINGS = createItem("elysium_leggings", new ElysiumArmourItem(ELYSIUM_MATERIAL, EquipmentSlot.LEGS, new Item.Settings()));
    static final Item ELYSIUM_BOOTS = createItem("elysium_boots", new ElysiumArmourItem(ELYSIUM_MATERIAL, EquipmentSlot.FEET, new Item.Settings()));
    static final Item EXPERIMENTAL_ELYSIUM_HELMET = createItem("experimental_elysium_helmet", new ElysiumArmourItem(ArmorMaterials.IRON, EquipmentSlot.HEAD, new Item.Settings()));
    static final Item EXPERIMENTAL_ELYSIUM_CHESTPLATE = createItem("experimental_elysium_chestplate", new ElysiumArmourItem(ArmorMaterials.IRON, EquipmentSlot.CHEST, new Item.Settings()));
    static final Item EXPERIMENTAL_ELYSIUM_LEGGINGS = createItem("experimental_elysium_leggings", new ElysiumArmourItem(ArmorMaterials.IRON, EquipmentSlot.LEGS, new Item.Settings()));
    static final Item EXPERIMENTAL_ELYSIUM_BOOTS = createItem("experimental_elysium_boots", new ElysiumArmourItem(ArmorMaterials.IRON, EquipmentSlot.FEET, new Item.Settings()));

    public static void init() {
        ITEMS.keySet().forEach((item) -> Registry.register(Registry.ITEM, ITEMS.get(item), item));

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (!level.isClient()) {
                ElysiumArmourComponent.KEY.maybeGet(player).ifPresent((comp) -> {
                    if (comp.shouldDischargeWhenAttacking() && entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) entity;
                        comp.dischargeVuln(living);
                    }
                });
            }
            return ActionResult.PASS;
        });
    }

    static <T extends Item> T createItemNoRegister(String name, T item) {
        ITEMS.put(item, Elysium.id(name));
        return item;
    }

    static <T extends Item> T createItem(String name, T item, Enchantment... enchantments) {
        ITEMS.put(item, Elysium.id(name));
        ELYSIUM_ITEMS.add(item.getDefaultStack());

        for(Enchantment enchantment : enchantments) {
            ItemStack stack = new ItemStack(item);
            stack.addEnchantment(enchantment, enchantment.getMaxLevel());
            ELYSIUM_ITEMS.add(stack);
        }

        return item;
    }
}
