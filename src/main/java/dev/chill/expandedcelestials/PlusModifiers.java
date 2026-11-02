package dev.chill.expandedcelestials;

import com.mojang.serialization.MapCodec;
import dev.corgitaco.enhancedcelestials2core.api.EnhancedCelestialsRegistry;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.modifier.LunarEventModifierType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** Adds our modifier type to the registry that Enhanced Celestials 2 owns, under our own namespace. */
public final class PlusModifiers {
    private static final DeferredRegister<LunarEventModifierType<?>> TYPES =
        DeferredRegister.create(EnhancedCelestialsRegistry.LUNAR_EVENT_MODIFIER_TYPE_KEY.location(), ExpandedCelestials.MODID);
    private static final MapCodec<PlayerAttributesModifier> SETTINGS = PlayerAttributesModifier.CODEC.fieldOf("settings");
    public static final LunarEventModifierType<PlayerAttributesModifier> PLAYER_ATTRIBUTES = () -> SETTINGS;
    static { TYPES.register("player_attributes", () -> PLAYER_ATTRIBUTES); }
    private PlusModifiers() {}
    public static void register(IEventBus modBus) { TYPES.register(modBus); }
}
