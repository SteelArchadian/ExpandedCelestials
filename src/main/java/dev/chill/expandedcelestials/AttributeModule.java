package dev.chill.expandedcelestials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraftforge.common.ForgeMod;
import java.util.*;

/** Never changes base values and never installs a saved/permanent modifier. */
public final class AttributeModule implements PlayerMechanic {
    private final Set<Attribute> touched = new HashSet<>();
    private record Desired(Attribute attribute, AttributeModifier modifier) {}
    @Override public void tick(ServerPlayer player, Rules.Profile profile) {
        Map<UUID, Desired> desired = new HashMap<>();
        if (profile != null) {
            multiplier(desired, "gravity", ForgeMod.ENTITY_GRAVITY.get(), profile.gravityMultiplier);
            for (Rules.AttributeRule rule : profile.attributes) {
                Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(new ResourceLocation(rule.attribute)).orElseThrow();
                ResourceLocation id = owned("custom/" + rule.key);
                desired.put(OwnedModifiers.uuid(id), new Desired(attribute, OwnedModifiers.modifier(id, rule.amount, operation(rule.operation))));
            }
        }
        desired.values().forEach(d -> touched.add(d.attribute));
        for (Attribute attribute : touched) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) continue;
            Map<UUID, AttributeModifier> forAttribute = new HashMap<>();
            desired.forEach((id, d) -> { if (d.attribute.equals(attribute)) forAttribute.put(id, d.modifier); });
            OwnedModifiers.reconcile(instance, forAttribute);
        }
    }
    /** Config files use the 1.21 names so one settings file works on every version. */
    static AttributeModifier.Operation operation(String name) {
        return switch (name) {
            case "ADD_VALUE" -> AttributeModifier.Operation.ADDITION;
            case "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> throw new IllegalArgumentException("Unknown attribute operation: " + name);
        };
    }
    private static void multiplier(Map<UUID, Desired> out, String key, Attribute attribute, double multiplier) {
        if (multiplier != 1.0) {
            ResourceLocation id = owned(key);
            out.put(OwnedModifiers.uuid(id), new Desired(attribute, OwnedModifiers.modifier(id, multiplier - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL)));
        }
    }
    private static ResourceLocation owned(String path) { return new ResourceLocation(GameplayPatch.ID, path); }
    /** Also scrubs leftover modifiers from older versions, without needing the old config. */
    @Override public void end(ServerPlayer player) {
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) OwnedModifiers.reconcile(instance, Map.of());
        }
        touched.clear();
    }
}
