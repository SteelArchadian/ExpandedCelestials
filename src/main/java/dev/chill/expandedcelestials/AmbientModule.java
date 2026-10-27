package dev.chill.expandedcelestials;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class AmbientModule {
    private record Weather(boolean rain, boolean thunder, int rainTime, int thunderTime, int clearTime) {
        static Weather capture(ServerLevelData data) { return new Weather(data.isRaining(), data.isThundering(), data.getRainTime(), data.getThunderTime(), data.getClearWeatherTime()); }
        void restore(ServerLevel level) {
            var data = level.getServer().getWorldData().overworldData();
            data.setRaining(rain); data.setThundering(thunder); data.setRainTime(rainTime);
            data.setThunderTime(thunderTime); data.setClearWeatherTime(clearTime);
        }
    }
    private final Map<ServerLevel, Weather> weather = new ConcurrentHashMap<>();
    private static final Item[] METEOR_ITEMS = {Items.STONE, Items.STONE, Items.ANDESITE, Items.DIORITE, Items.GRANITE,
        Items.RAW_IRON, Items.RAW_COPPER, Items.COAL, Items.RAW_GOLD};
    @SubscribeEvent public void tick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 20 != 0) return;
        Rules.Profile profile = EventBridge.profile(level);
        boolean storm = profile != null && profile.mechanics.storm && level.dimension().equals(Level.OVERWORLD)
            && level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE);
        if (!storm) { Weather previous = weather.remove(level); if (previous != null) previous.restore(level); }
        else if (level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE)) {
            weather.computeIfAbsent(level, l -> Weather.capture(l.getServer().getWorldData().overworldData()));
            if (!level.isThundering()) level.setWeatherParameters(0, 200, true, true);
        }
        if (profile == null) return;
        var rules = profile.mechanics;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            if (storm && level.isThundering() && level.random.nextDouble() < rules.lightningChance) {
                BlockPos strike = surface(level, player, 40);
                if (strike != null && level.canSeeSky(strike) && level.isRainingAt(strike)) {
                    var bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) { bolt.moveTo(strike.getX() + .5, strike.getY(), strike.getZ() + .5); level.addFreshEntity(bolt); }
                }
            }
            if (rules.meteorDropChance > 0 && level.random.nextDouble() < rules.meteorDropChance) {
                BlockPos pos = surface(level, player, 40);
                if (pos != null) {
                    var ground = level.getBlockState(pos.below());
                    if (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.SNOW_BLOCK) || ground.is(Blocks.SNOW)) {
                        var item = new ItemEntity(level, pos.getX()+.5, pos.getY()+.3, pos.getZ()+.5,
                            new ItemStack(METEOR_ITEMS[level.random.nextInt(METEOR_ITEMS.length)]));
                        item.setDefaultPickUpDelay(); level.addFreshEntity(item);
                        level.sendParticles(ParticleTypes.END_ROD, pos.getX()+.5, pos.getY()+2, pos.getZ()+.5, 8, .2, 1, .2, .01);
                    }
                }
            }
            // Ender atmosphere is generated locally with a fixed client budget.
            if (rules.mineralSparkles || rules.fungusSpreadChance > 0) {
                // Fixed budget; inspect already-loaded nearby blocks, never force chunk loads.
                for (int i=0; i<24; i++) {
                    BlockPos pos = player.blockPosition().offset(level.random.nextInt(17)-8, level.random.nextInt(7)-3, level.random.nextInt(17)-8);
                    if (!level.hasChunkAt(pos)) continue;
                    var state = level.getBlockState(pos);
                    if (rules.mineralSparkles && GrowthDropsModule.ore(state)) {
                        boolean exposed = false;
                        for (var direction : net.minecraft.core.Direction.values()) {
                            BlockPos adjacent = pos.relative(direction);
                            if (level.hasChunkAt(adjacent) && level.getBlockState(adjacent).isAir()) { exposed=true; break; }
                        }
                        if (exposed) level.sendParticles(ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,1,.5,.5,.5,0);
                    }
                    if (level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING)>0
                            && (state.is(Blocks.CRIMSON_FUNGUS) || state.is(Blocks.WARPED_FUNGUS)) && level.random.nextDouble() < rules.fungusSpreadChance) {
                        BlockPos to = pos.offset(level.random.nextInt(3)-1,0,level.random.nextInt(3)-1);
                        if (level.hasChunkAt(to) && level.isEmptyBlock(to)
                                && level.getBlockState(to.below()).is(net.minecraft.tags.BlockTags.NYLIUM) && state.canSurvive(level,to)) {
                            int nearby=0;
                            for (BlockPos check : BlockPos.betweenClosed(pos.offset(-2,0,-2),pos.offset(2,0,2)))
                                if (level.hasChunkAt(check) && level.getBlockState(check).getBlock()==state.getBlock()) nearby++;
                            if (nearby < 5) level.setBlock(to,state,3);
                        }
                    }
                }
            }
        }
    }
    private static BlockPos surface(ServerLevel level, ServerPlayer player, int radius) {
        BlockPos sample = player.blockPosition().offset(level.random.nextInt(radius*2+1)-radius,0,level.random.nextInt(radius*2+1)-radius);
        if (!level.hasChunkAt(sample)) return null;
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,sample);
    }
    @SubscribeEvent public void unload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) { Weather previous=weather.remove(level); if(previous!=null)previous.restore(level); }
    }
    @SubscribeEvent public void stop(ServerStoppingEvent event) {
        weather.entrySet().removeIf(e -> { if(e.getKey().getServer()!=event.getServer())return false;e.getValue().restore(e.getKey());return true; });
    }
}
