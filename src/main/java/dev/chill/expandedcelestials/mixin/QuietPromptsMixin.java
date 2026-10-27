package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.ECConfig;
import dev.corgitaco.enhancedcelestials2core.client.AddonPrompts;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Skips the join-time "install the other add-ons" chat messages. */
@Mixin(value=AddonPrompts.class,remap=false)
public abstract class QuietPromptsMixin {
    @Inject(method="notifyAll",at=@At("HEAD"),cancellable=true)
    private static void ecplus$quiet(Player player,CallbackInfo ci){if(ECConfig.on(ECConfig.QUIET_JOIN_MESSAGES))ci.cancel();}
}
