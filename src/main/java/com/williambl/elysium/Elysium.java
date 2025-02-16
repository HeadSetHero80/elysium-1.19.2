package com.williambl.elysium;

import com.mojang.serialization.Codec;
import com.williambl.elysium.armour.ElysiumArmourComponent;
import com.williambl.elysium.armour.ElysiumVulnerabilityMobEffect;
import com.williambl.elysium.cheirosiphon.CheirosiphonFlame;
import com.williambl.elysium.cheirosiphon.CheirosiphonItem;
import com.williambl.elysium.cheirosiphon.GhastlyFireball;
import com.williambl.elysium.cheirosiphon.HeatingItemsComponent;
import com.williambl.elysium.particles.ArcParticleOption;
import com.williambl.elysium.particles.MagneticWaveParticleOption;
import com.williambl.elysium.registry.ElysiumBlocks;
import com.williambl.elysium.registry.ElysiumEnchantments;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Elysium implements ModInitializer, EntityComponentInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Elysium");
	public static final TagKey<Block> ELYSIUM_FIRE_BASE_BLOCKS = TagKey.of(Registry.BLOCK.getKey(), id("elysium_fire_base_blocks"));
	public static final EntityType<CheirosiphonFlame> CHEIROSIPHON_FLAME = Registry.register(
			Registry.ENTITY_TYPE,
			id("cheirosiphon_flame"),
			FabricEntityTypeBuilder.<CheirosiphonFlame>create(SpawnGroup.MISC, (entityEntityType, world) -> {
						PlayerEntity user = null;
						return new CheirosiphonFlame(entityEntityType, user, world);
					})
					.dimensions(EntityDimensions.changing(0.25F, 1.0F))
					.trackRangeChunks(8)
					.trackedUpdateRate(20)
					.build()
	);

	public static final EntityType<GhastlyFireball> GHASTLY_FIREBALL = Registry.register(
			Registry.ENTITY_TYPE,
			id("ghastly_fireball"),
			FabricEntityTypeBuilder.<GhastlyFireball>create(SpawnGroup.MISC, GhastlyFireball::new)
					.dimensions(EntityDimensions.fixed(1.0F, 1.0F))
					.trackRangeChunks(8)
					.trackedUpdateRate(10)
					.build()
	);

	public static final DefaultParticleType ELYSIUM_FLAME_PARTICLE = (DefaultParticleType)Registry.register(Registry.PARTICLE_TYPE, id("elysium_flame"), new DefaultParticleType(false) {
	});
	public static final ParticleType<MagneticWaveParticleOption> MAGNETIC_WAVE_PARTICLE = (ParticleType)Registry.register(Registry.PARTICLE_TYPE, id("magnetic_wave"), new ParticleType<MagneticWaveParticleOption>(true, MagneticWaveParticleOption.DESERIALIZER) {
		@NotNull
		public Codec<MagneticWaveParticleOption> getCodec() {
			return MagneticWaveParticleOption.CODEC;
		}
	});
	public static final DefaultParticleType MAGNETIC_PULSE_PARTICLE = (DefaultParticleType)Registry.register(Registry.PARTICLE_TYPE, id("magnetic_pulse"), new DefaultParticleType(false) {
	});
	public static final ParticleType<ArcParticleOption> ARC_PARTICLE = (ParticleType)Registry.register(Registry.PARTICLE_TYPE, id("arc"), new ParticleType<ArcParticleOption>(false, ArcParticleOption.DESERIALIZER) {
		@NotNull
		public Codec<ArcParticleOption> getCodec() {
			return ArcParticleOption.CODEC;
		}
	});
	public static final StatusEffect ELYSIUM_VULNERABILITY = (StatusEffect)Registry.register(Registry.STATUS_EFFECT, id("elysium_vulnerability"), new ElysiumVulnerabilityMobEffect());

	public static Identifier id(String path) {
		return new Identifier("elysium", path);
	}

	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(HeatingItemsComponent.KEY, HeatingItemsComponent::new, RespawnCopyStrategy.INVENTORY);
		registry.registerForPlayers(ElysiumArmourComponent.KEY, ElysiumArmourComponent::new, RespawnCopyStrategy.LOSSLESS_ONLY);
		registry.registerFor(LivingEntity.class, ElysiumArmourComponent.KEY, ElysiumArmourComponent::new);
	}

	@Override
	public void onInitialize() {
		CheirosiphonItem.ServerboundAirblastPacket.init();
		ElysiumSounds.init();
		ElysiumEnchantments.init();
		ElysiumBlocks.init();
		ElysiumItems.init();
	}
}
