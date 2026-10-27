package dev.chill.expandedcelestials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public final class Selectors {
    private Selectors() {}
    public static boolean entity(List<String> selectors, EntityType<?> type) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return selectors.stream().anyMatch(s -> s.startsWith("#")
            ? type.is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(s.substring(1))))
            : matches(s, id));
    }
    public static boolean block(List<String> selectors, BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return selectors.stream().anyMatch(s -> s.startsWith("#")
            ? state.is(TagKey.create(Registries.BLOCK, ResourceLocation.parse(s.substring(1))))
            : matches(s, id));
    }
    private static boolean matches(String selector, ResourceLocation id) {
        return selector.endsWith(":*") ? id.getNamespace().equals(selector.substring(0, selector.length() - 2)) : id.toString().equals(selector);
    }
}
