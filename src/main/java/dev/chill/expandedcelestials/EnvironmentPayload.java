package dev.chill.expandedcelestials;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record EnvironmentPayload(ResourceLocation dimension,double gravity,boolean particles,boolean endSky,int particleRate) {
    public EnvironmentPayload {
        if(!Double.isFinite(gravity)||gravity<.1||gravity>4||particleRate<0||particleRate>64)throw new IllegalArgumentException("Invalid lunar environment");
    }
    public static void encode(EnvironmentPayload state,FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(state.dimension);buffer.writeDouble(state.gravity);
        buffer.writeBoolean(state.particles);buffer.writeBoolean(state.endSky);buffer.writeVarInt(state.particleRate);
    }
    public static EnvironmentPayload decode(FriendlyByteBuf buffer) {
        return new EnvironmentPayload(buffer.readResourceLocation(),buffer.readDouble(),buffer.readBoolean(),buffer.readBoolean(),buffer.readVarInt());
    }
}
