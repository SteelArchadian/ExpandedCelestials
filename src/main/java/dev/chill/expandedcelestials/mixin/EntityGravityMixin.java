package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.WorldGravity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Entity.class)
public abstract class EntityGravityMixin {
    @Inject(method="getGravity",at=@At("RETURN"),cancellable=true,remap=false)
    private void ecplus$worldGravity(CallbackInfoReturnable<Double> ci) {
        double original=ci.getReturnValue();double modified=WorldGravity.apply((Entity)(Object)this,original);
        if(modified!=original)ci.setReturnValue(modified);
    }
}
