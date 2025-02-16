package com.williambl.elysium.cheirosiphon;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.williambl.elysium.Elysium;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HeatingItemsComponent implements Component, CommonTickingComponent, AutoSyncedComponent {
    public static final ComponentKey<HeatingItemsComponent> KEY = ComponentRegistry.getOrCreate(Elysium.id("heating_items"), HeatingItemsComponent.class);
    private static final Codec<HeatingItem> HEATING_ITEM_CODEC = Registry.ITEM.getCodec().xmap((i) -> (HeatingItem)i, (i) -> (Item)i);
    private static final Codec<Map<HeatingItem, Integer>> HEATS_CODEC = Codec.unboundedMap(HEATING_ITEM_CODEC, Codec.INT);
    private static final Codec<Set<HeatingItem>> OVERHEATED_CODEC = HEATING_ITEM_CODEC.listOf().xmap(HashSet::new, ArrayList::new);
    private static final String HEATS_KEY = "heats";
    private static final String OVERHEATED_KEY = "overheated";
    private final Map<HeatingItem, Integer> heats = new HashMap();
    private final Set<HeatingItem> overheatedItems = new HashSet();
    private final PlayerEntity player;

    public HeatingItemsComponent(PlayerEntity player) {
        this.player = player;
    }

    public void readFromNbt(NbtCompound tag) {
        this.heats.clear();
        HEATS_CODEC.decode(NbtOps.INSTANCE, tag.getCompound("heats")).result().map(Pair::getFirst).ifPresent(this.heats::putAll);
        this.overheatedItems.clear();
        OVERHEATED_CODEC.decode(NbtOps.INSTANCE, tag.getCompound("overheated")).result().map(Pair::getFirst).ifPresent(this.overheatedItems::addAll);
    }

    public void writeToNbt(@NotNull NbtCompound tag) {
        HEATS_CODEC.encodeStart(NbtOps.INSTANCE, this.heats).result().ifPresent((t) -> tag.put("heats", t));
        OVERHEATED_CODEC.encodeStart(NbtOps.INSTANCE, this.overheatedItems).result().ifPresent((t) -> tag.put("overheated", t));
    }

    public void startHeating(HeatingItem heatingItem) {
        if (!(heatingItem instanceof Item)) {
            throw new IllegalArgumentException("%s is not an item!".formatted(heatingItem));
        } else {
            this.heats.putIfAbsent(heatingItem, 0);
            KEY.sync(this.player);
        }
    }

    public int getHeat(HeatingItem heatingItem) {
        return this.heats.getOrDefault(heatingItem, 0);
    }

    public void tick() {
        boolean needsSync = false;

        for (HeatingItem item : new ArrayList<>(this.heats.keySet())) {
            Hand hand = getHand(this.player, (Item) item);
            boolean isUsing = hand == this.player.getActiveHand() && this.player.isUsingItem();

            int newHeat = this.heats.compute(item, (k, v) -> {
                if (v == null) {
                    return 0;
                } else {
                    return HeatingItemHeatCallback.EVENT.invoker()
                            .getHeat(item, v, this.player, hand, isUsing)
                            .orElseGet(() -> item.getHeat(v, this.player, hand, isUsing));
                }
            });

            if (newHeat <= 0 && !isUsing) {
                this.heats.remove(item);
                this.overheatedItems.remove(item);
                needsSync = true;
            } else if (newHeat >= item.getMaxHeat()) {
                if (item instanceof HeatingItem) {
                    this.overheatedItems.add((HeatingItem) item);
                    needsSync = true;
                }
            }
        }

        if (needsSync && !this.player.getWorld().isClient()) {
            KEY.sync(this.player);
        }
    }

    @Nullable
    private static Hand getHand(PlayerEntity player, Item item) {
        if (player.getMainHandStack().getItem() == item) {
            return Hand.MAIN_HAND;
        } else {
            return player.getOffHandStack().getItem() == item ? Hand.OFF_HAND : null;
        }
    }

    public boolean isOverheated(HeatingItem heatingItem) {
        return this.overheatedItems.contains(heatingItem);
    }
}