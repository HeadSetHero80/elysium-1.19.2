package com.williambl.elysium;

import com.williambl.elysium.cheirosiphon.CheirosiphonItem;
import com.williambl.elysium.client.ElysiumPrismRenderer;
import com.williambl.elysium.client.GlowEffectManager;
import com.williambl.elysium.client.particle.ArcParticle;
import com.williambl.elysium.client.particle.ElysiumFlameParticle;
import com.williambl.elysium.client.particle.MagneticParticle;
import com.williambl.elysium.machine.prism.ElysiumPrismBlockEntity;
import com.williambl.elysium.registry.ElysiumBlocks;
import com.williambl.elysium.registry.ElysiumItems;
import com.williambl.elysium.registry.ElysiumSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ElysiumClient implements ClientModInitializer {
    public static boolean renderingGui;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(Elysium.CHEIROSIPHON_FLAME, EmptyEntityRenderer::new);
        EntityRendererRegistry.register(Elysium.GHASTLY_FIREBALL, (c) -> new FlyingItemEntityRenderer(c, 3.0F, true));
        BlockEntityRendererFactories.register(ElysiumBlocks.ELYSIUM_PRISM_BLOCK_ENTITY, (context) -> new ElysiumPrismRenderer());
        ModelPredicateProviderRegistry.register(ElysiumItems.CHEIROSIPHON, Elysium.id("held"), (stack, world, entity, seed) -> renderingGui ? 0.0F : 1.0F);
        ClientPlayNetworking.registerGlobalReceiver(CheirosiphonItem.ClientboundAirblastFxPacket.PACKET_ID, (client, handler, buf, responseSender) -> {
            Vec3d direction = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            Vec3d spawnPosition = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            client.execute(() -> {
                if (client.world != null) {
                    client.world.playSound(client.player, spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(), ElysiumSounds.CHEIROSIPHON_BLAST, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                    for (int i = 0; i < 3; ++i) {
                        client.world.addParticle(Elysium.MAGNETIC_PULSE_PARTICLE, spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(), direction.getX() * (double) ((float) (i + 1) / 3.0F), direction.getY() * (double) ((float) (i + 1) / 3.0F), direction.getZ() * (double) ((float) (i + 1) / 3.0F));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(ElysiumPrismBlockEntity.ClientboundPrismLaserPacket.PACKET_ID, (client, handler, buf, responseSender) -> {
            BlockPos pos = buf.readBlockPos();
            BlockPos endPos = (BlockPos) buf.readNullable(PacketByteBuf::readBlockPos);
            client.execute(() -> {
                if (client.world != null) {
                    BlockEntity entity = client.world.getBlockEntity(pos);
                    if (!(entity instanceof ElysiumPrismBlockEntity)) {
                        return;
                    }

                    ElysiumPrismBlockEntity prism = (ElysiumPrismBlockEntity) entity;
                    prism.setLaserEnd(endPos);
                }
            });
        });
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), new Block[]{ElysiumBlocks.ELYSIUM_FIRE});
        ParticleFactoryRegistry.getInstance().register(Elysium.ELYSIUM_FLAME_PARTICLE, ElysiumFlameParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(Elysium.MAGNETIC_WAVE_PARTICLE, MagneticParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(Elysium.MAGNETIC_PULSE_PARTICLE, MagneticParticle.SimpleProvider::new);
        ParticleFactoryRegistry.getInstance().register(Elysium.ARC_PARTICLE, new ArcParticle.Provider());
        GlowEffectManager.INSTANCE.init();
    }
}