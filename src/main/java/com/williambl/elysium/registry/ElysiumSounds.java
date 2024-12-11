package com.williambl.elysium.registry;

import com.williambl.elysium.Elysium;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ElysiumSounds {
    Map<SoundEvent, Identifier> SOUND_EVENTS = new LinkedHashMap<>();
    SoundEvent ELECTRODE_ZAP = createSoundEvent("block.electrode.zap");
    SoundEvent ELYSIUM_BREAK = createSoundEvent("block.elysium.break");
    SoundEvent ELYSIUM_PLACE = createSoundEvent("block.elysium.place");
    SoundEvent ELYSIUM_STEP = createSoundEvent("block.elysium.step");
    SoundEvent ELYSIUM_HIT = createSoundEvent("block.elysium.hit");
    SoundEvent ELYSIUM_FALL = createSoundEvent("block.elysium.fall");
    SoundEvent ELYSIUM_MACHINE_PLACE = createSoundEvent("block.elysium_machine.place");
    SoundEvent ELYSIUM_PRISM_PLACE = createSoundEvent("block.elysium_prism.place");
    SoundEvent ELYSIUM_PRISM_BREAK = createSoundEvent("block.elysium_prism.break");
    SoundEvent ELYSIUM_PRISM_LOOP = createSoundEvent("block.elysium_prism.loop");
    SoundEvent CHEIROSIPHON_DEACTIVATE = createSoundEvent("item.cheirosiphon.deactivate");
    SoundEvent CHEIROSIPHON_LOOP = createSoundEvent("item.cheirosiphon.loop");
    SoundEvent CHEIROSIPHON_BLAST = createSoundEvent("item.cheirosiphon.blast");
    SoundEvent CHEIROSIPHON_GHASTLY_BLAST = createSoundEvent("item.cheirosiphon.ghastly_blast");
    SoundEvent PARRY = createSoundEvent("entity.ghastly_fireball.parry");
    BlockSoundGroup ELYSIUM = new BlockSoundGroup(1.0F, 1.0F, ELYSIUM_BREAK, ELYSIUM_STEP, ELYSIUM_PLACE, ELYSIUM_HIT, ELYSIUM_FALL);
    BlockSoundGroup ELYSIUM_PRISM = new BlockSoundGroup(1.0F, 1.0F, ELYSIUM_PRISM_BREAK, ELYSIUM_STEP, ELYSIUM_PRISM_PLACE, ELYSIUM_HIT, ELYSIUM_FALL);

    static String getSubtitleKey(SoundEvent event) {
        return Util.createTranslationKey("subtitles", event.getId());
    }

    static void init() {
        SOUND_EVENTS.forEach((soundEvent, id) -> Registry.register(Registry.SOUND_EVENT, id, soundEvent));
    }

    static SoundEvent createSoundEvent(String name) {
        Identifier id = Elysium.id(name);
        SoundEvent soundEvent = new SoundEvent(id);
        SOUND_EVENTS.put(soundEvent, id);
        return soundEvent;
    }
}
