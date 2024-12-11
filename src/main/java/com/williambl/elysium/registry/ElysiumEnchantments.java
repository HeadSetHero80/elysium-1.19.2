package com.williambl.elysium.registry;

import com.williambl.elysium.Elysium;
import com.williambl.elysium.cheirosiphon.GhastlyEnchantment;
import com.williambl.elysium.cheirosiphon.JetEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ElysiumEnchantments {
    Map<Enchantment, Identifier> ENCHANTMENTS = new LinkedHashMap();
    Enchantment JET_ENCHANTMENT = createEnchantment("jet", new JetEnchantment());
    Enchantment GHASTLY_ENCHANTMENT = createEnchantment("ghastly", new GhastlyEnchantment());

    static void init() {
        ENCHANTMENTS.keySet().forEach((enchantment) -> Registry.register(Registry.ENCHANTMENT, (Identifier)ENCHANTMENTS.get(enchantment), enchantment));
    }

    static <T extends Enchantment> T createEnchantment(String name, T enchantment) {
        ENCHANTMENTS.put(enchantment, Elysium.id(name));
        return enchantment;
    }
}
