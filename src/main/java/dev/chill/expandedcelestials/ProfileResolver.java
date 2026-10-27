package dev.chill.expandedcelestials;

import java.util.List;

/** Keeps configuration precedence independent of the player lifecycle and EC registry. */
final class ProfileResolver {
    static Rules.Profile resolve(Rules.Profile override, PlayerAttributesModifier nativeRules, String dimension) {
        if (override != null && (!override.enabled || !override.dimensions.isEmpty() && !override.dimensions.contains(dimension))) return null;
        if (override == null && nativeRules == null) return null;
        Rules.Profile result = new Rules.Profile();
        result.gravityMultiplier = override != null && override.gravityMultiplier != null ? override.gravityMultiplier : nativeRules == null ? 1.0 : nativeRules.gravityMultiplier();
        result.miningSpeedMultiplier = override != null && override.miningSpeedMultiplier != null ? override.miningSpeedMultiplier : nativeRules == null ? 1.0 : nativeRules.miningSpeedMultiplier();
        result.attributes = override != null && override.attributes != null ? override.attributes : nativeRules == null ? List.of() : nativeRules.attributes();
        if (override != null) result.spawns = override.spawns;
        if (override != null) result.mechanics = override.mechanics;
        return result;
    }
}
