package dev.chill.expandedcelestials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.List;
import java.util.Map;

/** Reconciles one real Minecraft attribute while preserving all other mods' IDs. */
final class OwnedModifiers {
    static boolean owns(ResourceLocation id) {
        return id.getNamespace().equals(GameplayPatch.ID) || id.getNamespace().equals("lunar_mechanics");
    }
    static void reconcile(AttributeInstance instance, Map<ResourceLocation, AttributeModifier> desired) {
        for (AttributeModifier old : List.copyOf(instance.getModifiers())) {
            if (owns(old.id()) && !old.equals(desired.get(old.id()))) instance.removeModifier(old.id());
        }
        desired.forEach((id, modifier) -> {
            if (!id.getNamespace().equals(GameplayPatch.ID)) throw new IllegalArgumentException("Modifier is outside the patch's namespace: " + id);
            if (instance.getModifier(id) == null) instance.addTransientModifier(modifier);
        });
    }
}
