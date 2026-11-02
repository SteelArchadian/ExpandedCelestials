package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Nether mobs keep their form during the Eclipse theme. */
@Mixin(AbstractPiglin.class)
public abstract class AbstractPiglinFormMixin {
    @Inject(method="isImmuneToZombification",at=@At("RETURN"),cancellable=true)
    private void ecplus$keepNetherForm(CallbackInfoReturnable<Boolean> ci) {
        if(((Entity)(Object)this).level() instanceof ServerLevel level && EnvironmentModule.theme(level)==SpawnThemes.Theme.NETHER)ci.setReturnValue(true);
    }
}
