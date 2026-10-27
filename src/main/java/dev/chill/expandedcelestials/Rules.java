package dev.chill.expandedcelestials;

import java.util.*;
import java.util.regex.Pattern;

/** Pure configuration model, validated completely before it becomes active. */
public final class Rules {
    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+");
    public int schemaVersion = 4;
    public boolean enabled = true;
    public boolean allowOverworldGhasts = false;
    public Map<String, Profile> events = new LinkedHashMap<>();

    public static final class Profile {
        public boolean enabled = true;
        public List<String> dimensions = new ArrayList<>(List.of("minecraft:overworld"));
        // Null means inherit the EC2 event's native PLUS player_attributes modifier.
        public Double gravityMultiplier;
        public Double miningSpeedMultiplier;
        public List<AttributeRule> attributes;
        public Spawns spawns = new Spawns();
        public Mechanics mechanics = new Mechanics();
    }
    public static final class Mechanics {
        public boolean worldGravity;
        public boolean themedSpawns;
        public boolean enderSky;
        public int enderParticleRate = 32;
        public int hasteLevel;
        public boolean friendlyOnly;
        public double wildlifeGroupMultiplier = 1;
        public double cropGrowthChance;
        public double cropBonusChance;
        public double fungusSpreadChance;
        public double oreXpMultiplier = 1;
        public double oreBonusChance;
        public boolean mineralSparkles;
        public double fishingWaitMultiplier = 1;
        public boolean storm;
        public double lightningChance = 0.004;
        public double meteorDropChance;
        public boolean enderParticles;
        public boolean traderBoost;
        public boolean extraTraderGoods;
    }
    public static final class AttributeRule {
        public String key;
        public String attribute;
        public double amount;
        public String operation = "ADD_MULTIPLIED_TOTAL";
    }
    public static final class Spawns {
        public boolean enabled = true;
        public List<String> spawnTypes = new ArrayList<>(List.of("NATURAL", "CHUNK_GENERATION"));
        public List<String> excluded = new ArrayList<>();
        public List<String> removed = new ArrayList<>();
        public List<Replacement> replacements = new ArrayList<>();
    }
    public static final class Replacement {
        public String from;
        public String to;
    }
    public Rules validate() {
        require(schemaVersion >= 2 && schemaVersion <= 4, "Unsupported schemaVersion; Expanded Celestials uses schemaVersion 4");
        require(events != null, "events must be an object");
        events.forEach((event, p) -> {
            id(event); require(p != null, event + ": missing profile");
            list(p.dimensions).forEach(Rules::id);
            if (p.gravityMultiplier != null) range(p.gravityMultiplier, 0.1, 4, "gravityMultiplier");
            if (p.miningSpeedMultiplier != null) range(p.miningSpeedMultiplier, 0.1, 16, "miningSpeedMultiplier");
            if (p.attributes != null) validateAttributes(p.attributes);
            require(p.mechanics != null, "Missing mechanics object");
            Mechanics m = p.mechanics;
            require(m.enderParticleRate >= 0 && m.enderParticleRate <= 64, "enderParticleRate must be 0..64");
            require(m.hasteLevel >= 0 && m.hasteLevel <= 5, "hasteLevel must be 0..5");
            range(m.wildlifeGroupMultiplier, 1, 3, "wildlifeGroupMultiplier");
            range(m.cropGrowthChance, 0, 1, "cropGrowthChance");
            range(m.cropBonusChance, 0, 1, "cropBonusChance");
            range(m.fungusSpreadChance, 0, 1, "fungusSpreadChance");
            range(m.oreXpMultiplier, 1, 8, "oreXpMultiplier");
            range(m.oreBonusChance, 0, 1, "oreBonusChance");
            range(m.fishingWaitMultiplier, 0.1, 1, "fishingWaitMultiplier");
            range(m.lightningChance, 0, 0.1, "lightningChance");
            range(m.meteorDropChance, 0, 0.01, "meteorDropChance");
            require(p.spawns != null, "Missing spawns object");
            Spawns s = p.spawns;
            list(s.spawnTypes).forEach(t -> require(Set.of("NATURAL", "CHUNK_GENERATION", "SPAWNER", "STRUCTURE", "BREEDING", "MOB_SUMMONED", "JOCKEY", "EVENT", "CONVERSION", "REINFORCEMENT", "TRIGGERED", "BUCKET", "SPAWN_EGG", "COMMAND", "DISPENSER", "PATROL", "TRIAL_SPAWNER").contains(t), "Unknown spawn type: " + t));
            list(s.excluded).forEach(Rules::selector); list(s.removed).forEach(Rules::selector);
            for (Replacement r : list(s.replacements)) { require(r != null, "Null replacement"); selector(r.from); id(r.to); }
        });
        return this;
    }
    static void validateAttributes(List<AttributeRule> attributes) {
            Set<String> keys = new HashSet<>();
            for (AttributeRule a : list(attributes)) {
                require(a != null, "Null attribute"); id(a.attribute);
                require(a.key != null && a.key.matches("[a-z0-9_.-]+") && keys.add(a.key), "Attribute keys must be unique simple names");
                require(Double.isFinite(a.amount), "Non-finite attribute amount");
                require(Set.of("ADD_VALUE", "ADD_MULTIPLIED_BASE", "ADD_MULTIPLIED_TOTAL").contains(a.operation), "Unknown attribute operation");
            }
    }
    static void selector(String s) {
        require(s != null, "Null selector");
        if (s.startsWith("#")) id(s.substring(1));
        else if (s.endsWith(":*")) require(s.matches("[a-z0-9_.-]+:\\*"), "Invalid namespace wildcard: " + s);
        else id(s);
    }
    static void id(String s) { require(s != null && ID.matcher(s).matches(), "Expected namespace:path ID, got: " + s); }
    private static <T> List<T> list(List<T> list) { require(list != null, "Lists cannot be null"); return list; }
    private static void range(double n, double min, double max, String name) { require(Double.isFinite(n) && n >= min && n <= max, name + " must be in [" + min + ", " + max + "]"); }
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalArgumentException(message); }
}
