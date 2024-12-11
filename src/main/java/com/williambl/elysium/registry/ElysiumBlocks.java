package com.williambl.elysium.registry;

import com.mojang.serialization.Codec;
import com.williambl.elysium.Elysium;
import com.williambl.elysium.fire.ElysiumFireBlock;
import com.williambl.elysium.machine.electrode.ElectrodeBlock;
import com.williambl.elysium.machine.electrode.ElectrodeBlockEntity;
import com.williambl.elysium.machine.electrode.ElectrodeBlockItem;
import com.williambl.elysium.machine.gravitator.GravitatorBlock;
import com.williambl.elysium.machine.gravitator.GravitatorBlockEntity;
import com.williambl.elysium.machine.prism.ElysiumPrismBlock;
import com.williambl.elysium.machine.prism.ElysiumPrismBlockEntity;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryListCodec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public interface ElysiumBlocks {
    Map<Block, Identifier> BLOCKS = new LinkedHashMap<>();
    TagKey<Block> LIGHTNING_RODS = TagKey.of(Registry.BLOCK_KEY, Elysium.id("lightning_rods"));
    Map<Block, Integer> PRISM_POWERS = new HashMap<>();
    Map<Item, Double> ITEM_MAGNETISM = new HashMap<>();
    Map<Item, Double> ITEM_CONDUCTIVITY = new HashMap<>();
    Map<EntityType<?>, Double> ENTITY_MAGNETISM = new HashMap<>();
    Map<EntityType<?>, Double> ENTITY_CONDUCTIVITY = new HashMap<>();
    Property<Integer> ELYSIUM_POWER = IntProperty.of("elysium_power", 0, 4);
    Block ELYSIUM_FIRE = createBlockNoItem("elysium_fire", new ElysiumFireBlock(AbstractBlock.Settings.of(Material.FIRE, MapColor.LIGHT_BLUE).noCollision().breakInstantly().luminance(($) -> 10).sounds(BlockSoundGroup.WOOL)));
    Block ELYSIUM_BLOCK = createBlock("elysium_block", new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BROWN).requiresTool().strength(28.0F, 1200.0F).sounds(ElysiumSounds.ELYSIUM)));
    Block DIM_SELFLIT_REDSTONE_LAMP = createBlock("dim_selflit_redstone_lamp", new Block(AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP).luminance(($) -> 5)));
    Block SELFLIT_REDSTONE_LAMP = createBlock("selflit_redstone_lamp", new Block(AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP).luminance(($) -> 13)));
    Block BRILLIANT_SELFLIT_REDSTONE_LAMP = createBlock("brilliant_selflit_redstone_lamp", new Block(AbstractBlock.Settings.copy(Blocks.REDSTONE_LAMP).luminance(($) -> 15)));
    Block ELYSIUM_PRISM = createBlock("elysium_prism", new ElysiumPrismBlock(AbstractBlock.Settings.copy(ELYSIUM_BLOCK).sounds(ElysiumSounds.ELYSIUM_PRISM).luminance(ElysiumPrismBlock::lightLevel)));
    Block GRAVITATOR = createBlock("gravitator", new GravitatorBlock(AbstractBlock.Settings.copy(ELYSIUM_BLOCK).sounds(ElysiumSounds.ELYSIUM), false));
    Block REPULSOR = createBlock("repulsor", new GravitatorBlock(AbstractBlock.Settings.copy(ELYSIUM_BLOCK).sounds(ElysiumSounds.ELYSIUM), true));
    Block ELECTRODE = createBlock("electrode", new ElectrodeBlock(AbstractBlock.Settings.copy(ELYSIUM_BLOCK).sounds(ElysiumSounds.ELYSIUM)), (b) -> new ElectrodeBlockItem(b, new Item.Settings()));
    BlockEntityType<ElysiumPrismBlockEntity> ELYSIUM_PRISM_BLOCK_ENTITY = (BlockEntityType)Registry.register(Registry.BLOCK_ENTITY_TYPE, Elysium.id("elysium_prism"), FabricBlockEntityTypeBuilder.create(ElysiumPrismBlockEntity::new, new Block[0]).addBlock(ELYSIUM_PRISM).build());
    BlockEntityType<GravitatorBlockEntity> GRAVITATOR_BE = (BlockEntityType)Registry.register(Registry.BLOCK_ENTITY_TYPE, Elysium.id("gravitator"), FabricBlockEntityTypeBuilder.create(GravitatorBlockEntity::new, new Block[]{GRAVITATOR, REPULSOR}).build());
    BlockEntityType<ElectrodeBlockEntity> ELECTRODE_BE = (BlockEntityType)Registry.register(Registry.BLOCK_ENTITY_TYPE, Elysium.id("electrode"), FabricBlockEntityTypeBuilder.create(ElectrodeBlockEntity::new, new Block[]{ELECTRODE}).build());
    static void init() {
        BLOCKS.forEach((block, id) -> Registry.register(Registry.BLOCK, id, block));

        PRISM_POWERS.put(ELYSIUM_PRISM, 5);

        ENTITY_MAGNETISM.put(EntityType.ZOMBIE, 0.3);
        ENTITY_CONDUCTIVITY.put(EntityType.ZOMBIE, 0.2);
    }

    static <T extends Block> T createBlockNoItem(String name, T block) {
        BLOCKS.put(block, Elysium.id(name));
        return block;
    }

    @SafeVarargs
    static <T extends Block> T createBlock(String name, T block, Function<Block, Item>... consumers) {
        BLOCKS.put(block, Elysium.id(name));
        if (consumers.length == 0) {
            ElysiumItems.createItem(name, new BlockItem(block, new FabricItemSettings()));
        } else {
            for (Function<Block, Item> consumer : consumers) {
                ElysiumItems.createItem(name, consumer.apply(block));
            }
        }
        return block;
    }
}