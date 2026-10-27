package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.EventBridge;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WanderingTraderSpawner.class)
public abstract class TraderSpawnerMixin {
    @Shadow private int tickDelay;
    @Inject(method="tick",at=@At("HEAD"),remap=false)
    private void ecplus$moreAttempts(ServerLevel level,boolean enemies,boolean friendly,CallbackInfoReturnable<Integer> ci) {
        var profile=EventBridge.profile(level);
        if(profile!=null&&profile.mechanics.traderBoost && friendly && tickDelay>1
                && level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DO_TRADER_SPAWNING)
                && level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBSPAWNING)) tickDelay--;
    }
}
