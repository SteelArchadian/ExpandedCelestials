package dev.chill.expandedcelestials;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

final class MoonNames {
    static final List<String> NAMES = List.of("big_moon", "tiny_moon", "miner_moon", "blood_moon", "eclipse",
        "pink_moon", "grey_moon", "harvest_moon", "blue_moon", "green_moon", "tidal_moon", "hunters_moon",
        "howling_moon", "shroom_moon", "storm_moon", "meteor_shower_moon", "ender_moon", "wanderers_moon", "prospectors_moon");
    static final List<String> COMMAND_NAMES = java.util.stream.Stream.concat(NAMES.stream(), java.util.stream.Stream.of("pillager_moon")).toList();
    static ResourceLocation id(String name) {
        if(name.equals("pillager_moon"))name="grey_moon";
        if (name.contains(":")) return ResourceLocation.parse(name);
        return ResourceLocation.fromNamespaceAndPath(List.of("big_moon", "tiny_moon", "miner_moon").contains(name) ? "enhancedcelestials2core" : "enhancedcelestials_plus", name);
    }
}
