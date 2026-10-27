package dev.chill.expandedcelestials;

import dev.corgitaco.enhancedcelestials2core.EnhancedCelestials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/** The only code coupled to EC's lifecycle API. Never writes to its forecast. */
public final class EventBridge {
    private EventBridge() {}
    public static ResourceLocation current(ServerLevel level) {
        return EnhancedCelestials.lunarForecastWorldData(level)
            .flatMap(data -> data.currentLunarEventHolder().unwrapKey())
            .map(key -> key.location()).orElse(null);
    }
    public static Rules.Profile profile(ServerLevel level) {
        Rules rules = RuleStore.get();
        if (!rules.enabled) return null;
        var forecast = EnhancedCelestials.lunarForecastWorldData(level);
        if (forecast.isEmpty() || !forecast.get().isEventActive()) return null;
        var holder = forecast.get().currentLunarEventHolder();
        ResourceLocation key = holder.unwrapKey().map(k -> k.location()).orElse(null);
        Rules.Profile override = key == null ? null : rules.events.get(key.toString());
        var nativeRules = holder.value().getModifier(PlusModifiers.PLAYER_ATTRIBUTES).orElse(null);
        return ProfileResolver.resolve(override, nativeRules, level.dimension().location().toString());
    }
}
