package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.WorldGravity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
/** Falling blocks (sand, gravel, anvils) fall with the moon's gravity. */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockGravityMixin {
    @ModifyConstant(method="tick",constant=@Constant(doubleValue=-0.04D))
    private double ecplus$fallingGravity(double original){return -WorldGravity.apply((Entity)(Object)this,-original);}
}
