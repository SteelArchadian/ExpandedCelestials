package dev.chill.expandedcelestials;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GrowthDropsModule {
    static boolean ore(BlockState state) {
        if (state.is(TagKey.create(Registries.BLOCK, new ResourceLocation("forge:ores")))) return true;
        String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        return id.endsWith("_ore") || id.equals("ancient_debris");
    }
    @SubscribeEvent public void grow(BlockEvent.CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getState().getBlock() instanceof CropBlock)) return;
        Rules.Profile profile = EventBridge.profile(level);
        if (profile != null && event.getResult() == Event.Result.DEFAULT
                && level.random.nextDouble() < profile.mechanics.cropGrowthChance) event.setResult(Event.Result.ALLOW);
    }
    /** Ore XP bonus. (Extra item drops are added in {@link #oreDrops} through the Block.getDrops hook.) */
    @SubscribeEvent public void xp(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !ore(event.getState())) return;
        Rules.Profile profile = EventBridge.profile(level);
        if (profile == null || profile.mechanics.oreXpMultiplier <= 1) return;
        event.setExpToDrop((int)Math.min(Integer.MAX_VALUE, Math.round(event.getExpToDrop() * profile.mechanics.oreXpMultiplier)));
    }
    /** Chance of one extra item per eligible ore drop stack; never multiplies Silk Touch ore blocks. */
    public static java.util.List<net.minecraft.world.item.ItemStack> oreDrops(BlockState state,ServerLevel level,
            net.minecraft.world.entity.Entity breaker,java.util.List<net.minecraft.world.item.ItemStack> drops) {
        if (!(breaker instanceof Player) || !ore(state)) return drops;
        Rules.Profile profile = EventBridge.profile(level);
        if (profile == null || profile.mechanics.oreBonusChance <= 0) return drops;
        var result = new java.util.ArrayList<net.minecraft.world.item.ItemStack>(drops.size());
        for (var original : drops) {
            if (original.isEmpty() || original.getItem() instanceof BlockItem block && ore(block.getBlock().defaultBlockState())
                    || original.getCount() >= original.getMaxStackSize() || level.random.nextDouble() >= profile.mechanics.oreBonusChance) { result.add(original); continue; }
            var copy = original.copy(); copy.grow(1); result.add(copy);
        }
        return result;
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
