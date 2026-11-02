package dev.chill.expandedcelestials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Reconciles one real Minecraft attribute while preserving all other mods' modifiers. */
final class OwnedModifiers {
    private OwnedModifiers() {}
    /** Modifiers are identified by a UUID derived from our namespaced name (1.20.1 has no modifier IDs). */
    static UUID uuid(ResourceLocation id) { return UUID.nameUUIDFromBytes(("expandedcelestials:" + id).getBytes(StandardCharsets.UTF_8)); }
    static AttributeModifier modifier(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(uuid(id), id.toString(), amount, operation);
    }
    static boolean owns(AttributeModifier modifier) {
        String name = modifier.getName();
        return name.startsWith(GameplayPatch.ID + ":") || name.startsWith("lunar_mechanics:");
    }
    static void reconcile(AttributeInstance instance, Map<UUID, AttributeModifier> desired) {
        for (AttributeModifier old : List.copyOf(instance.getModifiers())) {
            if (owns(old) && !old.equals(desired.get(old.getId()))) instance.removeModifier(old.getId());
        }
        desired.forEach((id, modifier) -> {
            if (!modifier.getName().startsWith(GameplayPatch.ID + ":")) throw new IllegalArgumentException("Modifier is outside the add-on's namespace: " + modifier.getName());
            if (instance.getModifier(id) == null) instance.addTransientModifier(modifier);
        });
    }
}
