package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.EventBridge;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBolt.class)
public abstract class LightningMixin {
    @Inject(method="spawnFire",at=@At("HEAD"),cancellable=true,remap=false)
    private void ecplus$noStormFires(int count,CallbackInfo ci) {
        if (((LightningBolt)(Object)this).level() instanceof ServerLevel level) {
            var profile=EventBridge.profile(level);if(profile!=null&&profile.mechanics.storm)ci.cancel();
        }
    }
}
