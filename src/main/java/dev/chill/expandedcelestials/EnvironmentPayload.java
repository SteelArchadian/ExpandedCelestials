package dev.chill.expandedcelestials;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EnvironmentPayload(ResourceLocation dimension,double gravity,boolean particles,boolean endSky,int particleRate) implements CustomPacketPayload {
    public static final Type<EnvironmentPayload> TYPE=new Type<>(ResourceLocation.parse("expandedcelestials:environment"));
    public EnvironmentPayload {
        if(!Double.isFinite(gravity)||gravity<.1||gravity>4||particleRate<0||particleRate>64)throw new IllegalArgumentException("Invalid lunar environment");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf,EnvironmentPayload> CODEC=new StreamCodec<>() {
        public void encode(RegistryFriendlyByteBuf buffer,EnvironmentPayload state) {
            buffer.writeResourceLocation(state.dimension);buffer.writeDouble(state.gravity);
            buffer.writeBoolean(state.particles);buffer.writeBoolean(state.endSky);buffer.writeVarInt(state.particleRate);
        }
        public EnvironmentPayload decode(RegistryFriendlyByteBuf buffer) {
            return new EnvironmentPayload(buffer.readResourceLocation(),buffer.readDouble(),buffer.readBoolean(),buffer.readBoolean(),buffer.readVarInt());
        }
    };
    @Override public Type<EnvironmentPayload> type(){return TYPE;}
}
