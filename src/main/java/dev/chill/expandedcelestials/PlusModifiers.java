package dev.chill.expandedcelestials;

import com.mojang.serialization.MapCodec;
import dev.corgitaco.enhancedcelestials2core.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.modifier.LunarEventModifierType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Adds our modifier type to Enhanced Celestials' own registry, under our namespace. */
public final class PlusModifiers {
    private static final DeferredRegister<LunarEventModifierType<?>> TYPES =
        DeferredRegister.create(EnhancedCelestialsRegistry.LUNAR_EVENT_MODIFIER_TYPE, ExpandedCelestials.MODID);
    private static final MapCodec<PlayerAttributesModifier> SETTINGS = PlayerAttributesModifier.CODEC.fieldOf("settings");
    public static final LunarEventModifierType<PlayerAttributesModifier> PLAYER_ATTRIBUTES = () -> SETTINGS;
    static { TYPES.register("player_attributes", () -> PLAYER_ATTRIBUTES); }
    private PlusModifiers() {}
    public static void register(IEventBus modBus) { TYPES.register(modBus); }
}
