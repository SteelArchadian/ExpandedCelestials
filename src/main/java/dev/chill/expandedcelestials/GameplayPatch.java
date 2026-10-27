package dev.chill.expandedcelestials;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

public final class GameplayPatch {
    /** Namespace of the extra moons' event IDs (kept from the test build so existing worlds keep their calendar). */
    public static final String ID = "enhancedcelestials_plus";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static boolean installed;
    private GameplayPatch() {}
    public static synchronized void install(IEventBus modBus) {
        if (installed) return;
        PlusModifiers.register(modBus);
        installed = true;
        NeoForge.EVENT_BUS.register(new GameplayController());
        NeoForge.EVENT_BUS.register(new SpawnModule());
        NeoForge.EVENT_BUS.register(new GrowthDropsModule());
        NeoForge.EVENT_BUS.register(new AmbientModule());
        NeoForge.EVENT_BUS.register(new EnvironmentModule());
    }
}
