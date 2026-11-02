package dev.chill.expandedcelestials;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

public final class GameplayPatch {
    /** Namespace of the extra moons' event IDs (kept from the 1.21 build so settings carry over). */
    public static final String ID = "enhancedcelestials_plus";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static boolean installed;
    private GameplayPatch() {}
    public static synchronized void install(IEventBus modBus) {
        if (installed) return;
        PlusModifiers.register(modBus);
        installed = true;
        MinecraftForge.EVENT_BUS.register(new GameplayController());
        MinecraftForge.EVENT_BUS.register(new SpawnModule());
        MinecraftForge.EVENT_BUS.register(new GrowthDropsModule());
        MinecraftForge.EVENT_BUS.register(new AmbientModule());
        MinecraftForge.EVENT_BUS.register(new EnvironmentModule());
    }
}
