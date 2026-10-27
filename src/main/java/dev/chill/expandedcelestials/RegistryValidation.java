package dev.chill.expandedcelestials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

final class RegistryValidation {
    static void validate(Rules rules) {
        rules.events.values().forEach(p -> {
            if (p.attributes != null) attributes(p.attributes);
            entitySelectors(p.spawns.excluded); entitySelectors(p.spawns.removed);
            p.spawns.replacements.forEach(r -> { entitySelectors(List.of(r.from)); entity(r.to); });
        });
    }
    static void attributes(List<Rules.AttributeRule> rules) {
            rules.forEach(a -> {
                if (!BuiltInRegistries.ATTRIBUTE.containsKey(ResourceLocation.parse(a.attribute)))
                    throw new IllegalArgumentException("Unknown attribute: " + a.attribute);
                if (a.attribute.equals("minecraft:generic.gravity") || a.attribute.equals("minecraft:player.block_break_speed"))
                    throw new IllegalArgumentException("Use gravityMultiplier/miningSpeedMultiplier for " + a.attribute);
            });
    }
    private static void entitySelectors(List<String> selectors) {
        selectors.stream().filter(s -> !s.startsWith("#") && !s.endsWith(":*")).forEach(RegistryValidation::entity);
    }
    private static void entity(String id) {
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ResourceLocation.parse(id)))
            throw new IllegalArgumentException("Unknown entity: " + id);
    }
}
