package dev.chill.expandedcelestials;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class MoonNetwork {
    private static final String VERSION = "1";
    private static SimpleChannel channel;
    private MoonNetwork() {}
    static void register() {
        // Accepted whether or not the other side has the add-on, so vanilla clients can still join.
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(ExpandedCelestials.MODID, "environment"),
            () -> VERSION, v -> true, v -> true);
        channel.registerMessage(0, EnvironmentPayload.class, EnvironmentPayload::encode, EnvironmentPayload::decode,
            (payload, context) -> {
                context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientEnvironment.receive(payload)));
                context.get().setPacketHandled(true);
            }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void send(ServerPlayer player, EnvironmentPayload payload) {
        if (channel != null) channel.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
