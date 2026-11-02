package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.WorldGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
/** Dropped items fall with the moon's gravity (1.20.1 has no gravity method to hook, only this constant). */
@Mixin(ItemEntity.class)
public abstract class ItemGravityMixin {
    @ModifyConstant(method="tick",constant=@Constant(doubleValue=-0.04D))
    private double ecplus$itemGravity(double original){return -WorldGravity.apply((Entity)(Object)this,-original);}
}
