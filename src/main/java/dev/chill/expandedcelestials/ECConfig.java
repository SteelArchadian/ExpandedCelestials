package dev.chill.expandedcelestials;

import net.neoforged.neoforge.common.ModConfigSpec;

/** On/off switches for the add-on as a whole. Per-moon tuning lives in config/expandedcelestials.json. */
public final class ECConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue EXTRA_MOONS;
    public static final ModConfigSpec.BooleanValue MOONS_IN_RAIN;
    public static final ModConfigSpec.BooleanValue QUIET_JOIN_MESSAGES;
    static {
        var b = new ModConfigSpec.Builder();
        EXTRA_MOONS = b.comment("Add the 19 Expanded Celestials moons (Big, Tiny, Miner, Blood, Eclipse, Ender, ...).",
                "If you turn this off on an existing world, run /ecplus refreshForecast afterwards.")
            .define("extraMoons", true);
        MOONS_IN_RAIN = b.comment("Let moon events start and keep going while it rains (Enhanced Celestials normally needs clear skies).")
            .define("moonsInRain", true);
        QUIET_JOIN_MESSAGES = b.comment("Hide the Enhanced Celestials chat messages asking you to install its other add-ons when joining a world.")
            .define("quietJoinMessages", true);
        SPEC = b.build();
    }
    private ECConfig() {}
    /** Safe to call before the config file is loaded (returns the default). */
    public static boolean on(ModConfigSpec.BooleanValue value) {
        return !SPEC.isLoaded() || value.get();
    }
}
