package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.TraderGoods;
import net.minecraft.world.entity.npc.WanderingTrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class TraderMixin {
    @Inject(method="updateTrades",at=@At("TAIL"),remap=false)
    private void ecplus$goods(CallbackInfo ci){TraderGoods.add((WanderingTrader)(Object)this);}
}
