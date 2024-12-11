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

public record ArcParticleOption(double targetX, double targetY, double targetZ, float scale, int entityId) implements ParticleEffect {
    public static final Codec<ArcParticleOption> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Codec.DOUBLE.fieldOf("targetX").forGetter(ArcParticleOption::targetX), Codec.DOUBLE.fieldOf("targetY").forGetter(ArcParticleOption::targetY), Codec.DOUBLE.fieldOf("targetZ").forGetter(ArcParticleOption::targetZ), Codec.FLOAT.fieldOf("scale").forGetter(ArcParticleOption::scale), Codec.INT.optionalFieldOf("entity_id", 0).forGetter(ArcParticleOption::entityId)).apply(instance, ArcParticleOption::new));
    public static final ParticleEffect.Factory<ArcParticleOption> DESERIALIZER = new ParticleEffect.Factory<ArcParticleOption>() {

        @NotNull
        public ArcParticleOption read(@NotNull ParticleType<ArcParticleOption> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            double x = stringReader.readDouble();
            stringReader.expect(' ');
            double y = stringReader.readDouble();
            stringReader.expect(' ');
            double z = stringReader.readDouble();
            stringReader.expect(' ');
            float s = stringReader.readFloat();
            stringReader.expect(' ');
            int i = stringReader.readInt();
            return new ArcParticleOption(x, y, z, s, i);
        }

        @NotNull
        public ArcParticleOption read(@NotNull ParticleType<ArcParticleOption> particleType, PacketByteBuf friendlyByteBuf) {
            return new ArcParticleOption(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readFloat(), friendlyByteBuf.readVarInt());
        }
    };

    public ArcParticleOption(double x, double y, double z) {
        this(x, y, z, 1.0F, 0);
    }

    @NotNull
    public ParticleType<?> getType() {
        return Elysium.ARC_PARTICLE;
    }

    public void write(PacketByteBuf buffer) {
        buffer.writeDouble(this.targetX);
        buffer.writeDouble(this.targetY);
        buffer.writeDouble(this.targetZ);
        buffer.writeFloat(this.scale);
        buffer.writeVarInt(this.entityId);
    }

    @NotNull
    public String asString() {
        return "%s %f %f %f %f".formatted(Registry.PARTICLE_TYPE.getId(this.getType()), this.targetX(), this.targetY(), this.targetZ(), this.scale());
    }
}
