package com.williambl.elysium.client;

import com.williambl.elysium.registry.ElysiumBlocks;
import com.williambl.elysium.registry.ElysiumSounds;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ElysiumPrismSoundInstance extends MovingSoundInstance {
    private static ElysiumPrismSoundInstance instance = null;
    private BlockPos prismPos = BlockPos.ORIGIN;

    public static void play(BlockPos pos) {
        SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
        if (instance != null && soundManager.isPlaying(instance) && !instance.isDone()) {
            Entity cameraEntity = MinecraftClient.getInstance().cameraEntity;
            if (cameraEntity == null) {
                return;
            }

            double distance = cameraEntity.getPos().distanceTo(Vec3d.ofCenter(pos));
            double distanceOfCurrent = cameraEntity.getPos().distanceTo(Vec3d.ofCenter(instance.prismPos));
            if (distance < distanceOfCurrent) {
                instance.setPrismPos(pos);
            }
        } else {
            instance = new ElysiumPrismSoundInstance(ElysiumSounds.ELYSIUM_PRISM_LOOP, SoundCategory.BLOCKS, pos);
            soundManager.play(instance);
        }

    }

    public ElysiumPrismSoundInstance(SoundEvent soundEvent, SoundCategory soundSource, BlockPos prismPos) {
        super(soundEvent, soundSource, SoundInstance.createRandom());
        this.repeat = true;
        this.setPrismPos(prismPos);
    }

    private void setPrismPos(BlockPos pos) {
        this.prismPos = pos;
        this.x = (double)pos.getX() + 0.5D;
        this.y = (double)pos.getY() + 0.5D;
        this.z = (double)pos.getZ() + 0.5D;
    }

    public void tick() {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level != null) {
            BlockState state = level.getBlockState(this.prismPos);
            if (state.getBlock() != ElysiumBlocks.ELYSIUM_PRISM || state.get(ElysiumBlocks.ELYSIUM_POWER) < 1) {
                this.setDone();
            }
        }

    }
}
