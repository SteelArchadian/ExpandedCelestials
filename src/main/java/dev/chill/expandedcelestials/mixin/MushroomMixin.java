package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.EventBridge;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(MushroomBlock.class)
public abstract class MushroomMixin {
    @Redirect(method="randomTick",at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextInt(I)I",ordinal=0),remap=false)
    private int ecplus$spread(RandomSource random,int bound,BlockState state,ServerLevel level,BlockPos pos,RandomSource tickRandom) {
        var profile=EventBridge.profile(level);
        return random.nextInt(profile!=null&&profile.mechanics.fungusSpreadChance>0 ? Math.max(1,(int)Math.round(bound/(1+3*profile.mechanics.fungusSpreadChance))) : bound);
    }
}
