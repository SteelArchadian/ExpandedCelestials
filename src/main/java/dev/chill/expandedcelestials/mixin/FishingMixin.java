package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.EventBridge;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHook.class)
public abstract class FishingMixin {
    @Shadow private int timeUntilLured;
    @Unique private double ecplus$extraProgress;
    @Inject(method="catchingFish",at=@At("HEAD"),remap=false)
    private void ecplus$fasterBites(BlockPos pos,CallbackInfo ci) {
        FishingHook hook=(FishingHook)(Object)this;
        if (!(hook.level() instanceof ServerLevel level)) return;
        var profile=EventBridge.profile(level);
        var biome=level.getBiome(pos);
        if(profile==null || !(biome.is(BiomeTags.IS_OCEAN)||biome.is(BiomeTags.IS_RIVER)) || timeUntilLured<=1){ecplus$extraProgress=0;return;}
        ecplus$extraProgress+=1/profile.mechanics.fishingWaitMultiplier-1;
        int extra=(int)ecplus$extraProgress;
        timeUntilLured=Math.max(1,timeUntilLured-extra);ecplus$extraProgress-=extra;
    }
}
