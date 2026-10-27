package dev.chill.expandedcelestials.mixin;
import dev.chill.expandedcelestials.EnvironmentModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(NaturalSpawner.class)
public abstract class SurfaceSpawnMixin {
    @Unique private static final ThreadLocal<MobCategory> ecplus$category=new ThreadLocal<>();
    @Inject(method="spawnCategoryForChunk",at=@At("HEAD"),remap=false)
    private static void ecplus$category(MobCategory category,ServerLevel level,LevelChunk chunk,NaturalSpawner.SpawnPredicate check,NaturalSpawner.AfterSpawnCallback after,CallbackInfo ci){ecplus$category.set(category);}
    @Inject(method="spawnCategoryForChunk",at=@At("RETURN"),remap=false)
    private static void ecplus$clearCategory(MobCategory category,ServerLevel level,LevelChunk chunk,NaturalSpawner.SpawnPredicate check,NaturalSpawner.AfterSpawnCallback after,CallbackInfo ci){ecplus$category.set(null);}
    @Inject(method="getRandomPosWithin",at=@At("RETURN"),cancellable=true,remap=false)
    private static void ecplus$visibleSpawns(Level world,LevelChunk chunk,CallbackInfoReturnable<BlockPos> ci) {
        if(ecplus$category.get()!=MobCategory.MONSTER || !(world instanceof ServerLevel level)
                || EnvironmentModule.theme(level)==null || level.random.nextDouble()>=.65)return;
        BlockPos original=ci.getReturnValue();
        int surface=chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,original.getX()&15,original.getZ()&15)+1;
        var player=level.getNearestPlayer(original.getX()+.5,surface,original.getZ()+.5,-1,false);
        if(player!=null&&!player.isSpectator()&&player.getY()>=surface-8)
            ci.setReturnValue(new BlockPos(original.getX(),surface,original.getZ()));
    }
}
