package dev.chill.expandedcelestials;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber(modid=ExpandedCelestials.MODID,bus=EventBusSubscriber.Bus.MOD)
public final class MoonNetwork {
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(EnvironmentPayload.TYPE,EnvironmentPayload.CODEC,
            (payload,context)->ClientEnvironment.receive(payload));
    }
}
