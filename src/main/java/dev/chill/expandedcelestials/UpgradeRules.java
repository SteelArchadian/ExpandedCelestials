package dev.chill.expandedcelestials;

/** Upgrade the earlier test defaults without discarding deliberate custom profiles. */
final class UpgradeRules {
    static Rules upgrade(Rules old, Rules defaults) {
        defaults.enabled = old.enabled;
        defaults.allowOverworldGhasts = old.allowOverworldGhasts;
        old.events.forEach((id, profile) -> {
            Rules.Profile proposed = defaults.events.get(id);
            if (proposed != null) {
                if (id.equals("enhancedcelestials2core:big_moon") && (Double.valueOf(.85).equals(profile.gravityMultiplier) || Double.valueOf(.65).equals(profile.gravityMultiplier))) profile.gravityMultiplier = proposed.gravityMultiplier;
                if (id.equals("enhancedcelestials2core:tiny_moon") && Double.valueOf(1.15).equals(profile.gravityMultiplier)) profile.gravityMultiplier = proposed.gravityMultiplier;
                if (id.equals("enhancedcelestials2core:miner_moon") && Double.valueOf(1.35).equals(profile.miningSpeedMultiplier)) profile.miningSpeedMultiplier = 1.0;
                if (old.schemaVersion == 2) profile.mechanics = proposed.mechanics;
                else {
                    profile.mechanics.worldGravity = proposed.mechanics.worldGravity;
                    profile.mechanics.themedSpawns = proposed.mechanics.themedSpawns;
                    profile.mechanics.enderSky = proposed.mechanics.enderSky;
                    profile.mechanics.enderParticleRate = proposed.mechanics.enderParticleRate;
                }
            }
            defaults.events.put(id, profile);
        });
        return defaults.validate();
    }
}
