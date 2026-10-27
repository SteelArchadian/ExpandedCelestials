package dev.chill.expandedcelestials;

import net.neoforged.fml.loading.FMLPaths;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Objects;

public final class RuleStore {
    private static volatile Rules active = new Rules();
    private static volatile long revision;
    private RuleStore() {}
    public static Rules get() { return active; }
    public static long revision() { return revision; }
    public static Path path() { return FMLPaths.CONFIGDIR.get().resolve("expandedcelestials.json"); }
    public static void reload() throws IOException {
        Path legacy = path().resolveSibling("enhancedcelestials-plus.json");
        if (!Files.exists(path()) && Files.exists(legacy)) {
            // Carry over settings from the old all-in-one test build.
            Files.copy(legacy, path());
            GameplayPatch.LOGGER.info("Copied settings from {} to {}", legacy, path());
        }
        if (!Files.exists(path())) {
            Files.createDirectories(path().getParent());
            try (InputStream in = Objects.requireNonNull(RuleStore.class.getResourceAsStream("/default-expandedcelestials.json"))) {
                Files.copy(in, path());
            }
        }
        Rules candidate = ConfigParser.parse(Files.readString(path(), StandardCharsets.UTF_8));
        if (candidate.schemaVersion < 4) {
            Rules defaults;
            try (InputStream in = Objects.requireNonNull(RuleStore.class.getResourceAsStream("/default-expandedcelestials.json"))) {
                defaults = ConfigParser.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
            candidate = UpgradeRules.upgrade(candidate, defaults);
            RegistryValidation.validate(candidate);
            Path backup = path().resolveSibling("expandedcelestials.pre-schema4.json");
            if (!Files.exists(backup)) Files.copy(path(), backup);
            Path temporary = path().resolveSibling("expandedcelestials.upgrade.tmp");
            Files.writeString(temporary, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(candidate));
            try { Files.move(temporary, path(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path(), StandardCopyOption.REPLACE_EXISTING); }
            GameplayPatch.LOGGER.info("Upgraded Expanded Celestials config to schema 4; previous config saved at {}", backup);
        }
        RegistryValidation.validate(candidate);
        active = candidate;
        revision++;
    }
}
