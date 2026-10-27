package dev.chill.expandedcelestials;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.modifier.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** EC2-native datapack modifier; server configuration can override it per event. */
public record PlayerAttributesModifier(double gravityMultiplier, double miningSpeedMultiplier, List<Rules.AttributeRule> attributes) implements LunarEventModifier {
    private static Codec<Double> multiplier(double low, double high) {
        return Codec.DOUBLE.validate(n -> Double.isFinite(n) && n >= low && n <= high
            ? DataResult.success(n) : DataResult.error(() -> "Multiplier must be finite and in [" + low + ", " + high + "]"));
    }
    private static final Codec<Rules.AttributeRule> ATTRIBUTE_CODEC = RecordCodecBuilder.create(b -> b.group(
        Codec.STRING.fieldOf("key").forGetter((Rules.AttributeRule r) -> r.key),
        ResourceLocation.CODEC.fieldOf("attribute").forGetter((Rules.AttributeRule r) -> ResourceLocation.parse(r.attribute)),
        Codec.DOUBLE.fieldOf("amount").forGetter((Rules.AttributeRule r) -> r.amount),
        Codec.STRING.optionalFieldOf("operation", "ADD_MULTIPLIED_TOTAL").forGetter((Rules.AttributeRule r) -> r.operation)
    ).apply(b, (key, attribute, amount, operation) -> {
        Rules.AttributeRule r = new Rules.AttributeRule(); r.key = key; r.attribute = attribute.toString(); r.amount = amount; r.operation = operation; return r;
    }));
    private static final MapCodec<PlayerAttributesModifier> RAW_CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
        multiplier(0.1, 4).optionalFieldOf("gravity_multiplier", 1.0).forGetter(PlayerAttributesModifier::gravityMultiplier),
        multiplier(0.1, 16).optionalFieldOf("mining_speed_multiplier", 1.0).forGetter(PlayerAttributesModifier::miningSpeedMultiplier),
        ATTRIBUTE_CODEC.listOf().optionalFieldOf("attributes", List.of()).forGetter(PlayerAttributesModifier::attributes)
    ).apply(b, PlayerAttributesModifier::new));
    public static final MapCodec<PlayerAttributesModifier> CODEC = RAW_CODEC.flatXmap(value -> {
        try { Rules.validateAttributes(value.attributes); RegistryValidation.attributes(value.attributes); return DataResult.success(value); }
        catch (IllegalArgumentException e) { return DataResult.error(e::getMessage); }
    }, DataResult::success);
    @Override public LunarEventModifierType<?> type() { return PlusModifiers.PLAYER_ATTRIBUTES; }
    @Override public Component description() {
        return Component.literal("Expanded Celestials player attributes: gravity " + gravityMultiplier + "x, mining " + miningSpeedMultiplier + "x");
    }
}
