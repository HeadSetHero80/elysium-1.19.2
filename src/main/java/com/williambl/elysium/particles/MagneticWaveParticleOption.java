package com.williambl.elysium.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.williambl.elysium.Elysium;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public record MagneticWaveParticleOption(boolean isReversed, float distance, float widthScale) implements ParticleEffect {
    public static final Codec<MagneticWaveParticleOption> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.BOOL.fieldOf("reversed").forGetter(MagneticWaveParticleOption::isReversed), Codec.FLOAT.fieldOf("distance").forGetter(MagneticWaveParticleOption::distance), Codec.FLOAT.fieldOf("width_scale").forGetter(MagneticWaveParticleOption::widthScale)).apply(instance, MagneticWaveParticleOption::new));
    public static final ParticleEffect.Factory<MagneticWaveParticleOption> DESERIALIZER = new ParticleEffect.Factory<MagneticWaveParticleOption>() {

        @NotNull
        public MagneticWaveParticleOption read(@NotNull ParticleType<MagneticWaveParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            boolean b = stringReader.readBoolean();
            stringReader.expect(' ');
            float f1 = stringReader.readFloat();
            stringReader.expect(' ');
            float f2 = stringReader.readFloat();
            return new MagneticWaveParticleOption(b, f1, f2);
        }

        @NotNull
        public MagneticWaveParticleOption read(@NotNull ParticleType<MagneticWaveParticleOption> particleType, PacketByteBuf friendlyByteBuf) {
            return new MagneticWaveParticleOption(friendlyByteBuf.readBoolean(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat());
        }
    };

    @NotNull
    public ParticleType<?> getType() {
        return Elysium.MAGNETIC_WAVE_PARTICLE;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeBoolean(this.isReversed);
        buffer.writeFloat(this.distance);
        buffer.writeFloat(this.widthScale);
    }

    @NotNull
    public String asString() {
        return "%s %b %f %f".formatted(Registry.PARTICLE_TYPE.getId(this.getType()), this.isReversed, this.distance, this.widthScale);
    }
}