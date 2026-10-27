package dev.chill.expandedcelestials.mixin;
import java.util.List;
import dev.chill.expandedcelestials.GrowthDropsModule;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Block.class)
public abstract class CropDropsMixin {
    @Inject(method="getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at=@At("RETURN"),cancellable=true,remap=false)
    private static void ecplus$cropYield(BlockState state,ServerLevel level,BlockPos pos,BlockEntity blockEntity,Entity entity,ItemStack tool,CallbackInfoReturnable<List<ItemStack>> ci) {
        var original=ci.getReturnValue();
        var modified=GrowthDropsModule.cropDrops(state,level,entity,original);
        if(modified!=original)ci.setReturnValue(modified);
    }
}
