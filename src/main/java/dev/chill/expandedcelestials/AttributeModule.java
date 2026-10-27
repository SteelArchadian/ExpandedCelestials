package dev.chill.expandedcelestials;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.*;

/** Never changes base values and never installs a saved/permanent modifier. */
public final class AttributeModule implements PlayerMechanic {
    private final Set<Holder<Attribute>> touched = new HashSet<>();
    private record Desired(Holder<Attribute> attribute, AttributeModifier modifier) {}
    @Override public void tick(ServerPlayer player, Rules.Profile profile) {
        Map<ResourceLocation, Desired> desired = new HashMap<>();
        if (profile != null) {
            multiplier(desired, "gravity", Attributes.GRAVITY, profile.gravityMultiplier);
            multiplier(desired, "mining_speed", Attributes.BLOCK_BREAK_SPEED, profile.miningSpeedMultiplier);
            for (Rules.AttributeRule rule : profile.attributes) {
                Holder<Attribute> attribute = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(rule.attribute)).orElseThrow();
                ResourceLocation id = owned("custom/" + rule.key);
                desired.put(id, new Desired(attribute, new AttributeModifier(id, rule.amount, AttributeModifier.Operation.valueOf(rule.operation))));
            }
        }
        desired.values().forEach(d -> touched.add(d.attribute));
        for (Holder<Attribute> holder : touched) {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance == null) continue;
            Map<ResourceLocation, AttributeModifier> forAttribute = new HashMap<>();
            desired.forEach((id, d) -> { if (d.attribute.equals(holder)) forAttribute.put(id, d.modifier); });
            OwnedModifiers.reconcile(instance, forAttribute);
        }
    }
    private static void multiplier(Map<ResourceLocation, Desired> out, String key, Holder<Attribute> attribute, double multiplier) {
        if (multiplier != 1.0) {
            ResourceLocation id = owned(key);
            out.put(id, new Desired(attribute, new AttributeModifier(id, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)));
        }
    }
    private static ResourceLocation owned(String path) { return ResourceLocation.fromNamespaceAndPath(GameplayPatch.ID, path); }
    /** Also scrubs old addon versions' saved modifiers, without needing the old config. */
    @Override public void end(ServerPlayer player) {
        BuiltInRegistries.ATTRIBUTE.holders().forEach(holder -> {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance != null) {
                OwnedModifiers.reconcile(instance, Map.of());
            }
        });
        touched.clear();
    }
}
