package dev.chill.expandedcelestials;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

public final class GrowthDropsModule {
    static boolean ore(BlockState state) {
        if (state.is(TagKey.create(Registries.BLOCK, ResourceLocation.parse("c:ores")))) return true;
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return id.endsWith("_ore") || id.equals("ancient_debris");
    }
    @SubscribeEvent public void grow(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getState().getBlock() instanceof CropBlock)) return;
        Rules.Profile profile = EventBridge.profile(level);
        if (profile != null && event.getResult() == CropGrowEvent.Pre.Result.DEFAULT
                && level.random.nextDouble() < profile.mechanics.cropGrowthChance) event.setResult(CropGrowEvent.Pre.Result.GROW);
    }
    @SubscribeEvent public void drops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player)) return;
        ServerLevel level = event.getLevel();
        Rules.Profile profile = EventBridge.profile(level);
        if (profile == null) return;
        Rules.Mechanics rules = profile.mechanics;
        boolean isOre = ore(event.getState());
        if (isOre && rules.oreXpMultiplier > 1) event.setDroppedExperience((int)Math.min(Integer.MAX_VALUE,
            Math.round(event.getDroppedExperience() * rules.oreXpMultiplier)));
        double chance = isOre ? rules.oreBonusChance : 0;
        if (chance <= 0) return;
        for (var drop : event.getDrops()) {
            var stack = drop.getItem();
            if (stack.isEmpty() || stack.getItem() instanceof BlockItem block && ore(block.getBlock().defaultBlockState())) continue;
            // Add one extra unit probabilistically; never multiply Silk Touch ore blocks.
            if (level.random.nextDouble() < chance && stack.getCount() < stack.getMaxStackSize()) stack.grow(1);
        }
    }
    /** Block.getDrops is also used by Actually Harvest's right-click path. */
    public static java.util.List<net.minecraft.world.item.ItemStack> cropDrops(BlockState state,ServerLevel level,
            net.minecraft.world.entity.Entity breaker,java.util.List<net.minecraft.world.item.ItemStack> drops) {
        if (!(breaker instanceof Player) || !(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return drops;
        Rules.Profile profile=EventBridge.profile(level);
        return profile==null ? drops : bonusCopies(drops,profile.mechanics.cropBonusChance,level.random::nextDouble);
    }
    static java.util.List<net.minecraft.world.item.ItemStack> bonusCopies(java.util.List<net.minecraft.world.item.ItemStack> drops,
            double chance,java.util.function.DoubleSupplier random) {
        if (chance<=0) return drops;
        var result=new java.util.ArrayList<net.minecraft.world.item.ItemStack>(drops.size());
        for(var original:drops) {
            if(!original.isEmpty() && original.getCount()<original.getMaxStackSize() && random.getAsDouble()<chance) {
                var copy=original.copy();copy.grow(1);result.add(copy);
            } else result.add(original);
        }
        return result;
    }
}
