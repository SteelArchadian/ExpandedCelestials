package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.ECConfig;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.LunarDimensionSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Moons also happen in rain (Storm Moon would otherwise end itself). */
@Mixin(value=LunarDimensionSettings.class,remap=false)
public abstract class ClearSkiesMixin {
    @Inject(method="requiresClearSkies",at=@At("RETURN"),cancellable=true)
    private void ecplus$allowRain(CallbackInfoReturnable<Boolean> ci){if(ci.getReturnValueZ()&&ECConfig.on(ECConfig.MOONS_IN_RAIN))ci.setReturnValue(false);}
}
