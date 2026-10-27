package dev.chill.expandedcelestials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import java.util.*;

public final class SpawnModule {
    private static final Map<String, List<MobSpawnSettings.SpawnerData>> MOON_SPAWNS = Map.of(
        "eclipse", List.of(spawn(EntityType.BLAZE,8,1,2), spawn(EntityType.WITHER_SKELETON,8,1,2),
            spawn(EntityType.MAGMA_CUBE,8,1,2), spawn(EntityType.ZOMBIFIED_PIGLIN,12,1,3),
            spawn(EntityType.PIGLIN,8,1,2), spawn(EntityType.HOGLIN,5,1,2)),
        "grey_moon", List.of(spawn(EntityType.PILLAGER,30,1,3)),
        "green_moon", List.of(spawn(EntityType.SLIME,60,1,3)),
        "ender_moon", List.of(spawn(EntityType.ENDERMAN,70,1,3),spawn(EntityType.ENDERMITE,3,1,1)));
    private static MobSpawnSettings.SpawnerData spawn(EntityType<?> type,int weight,int min,int max) {
        return new MobSpawnSettings.SpawnerData(type,weight,min,max);
    }
    private static boolean ghastBlocked(ServerLevel level, EntityType<?> type) {
        return RuleStore.get().enabled && !RuleStore.get().allowOverworldGhasts && level.dimension().equals(Level.OVERWORLD) && type == EntityType.GHAST;
    }
    private static boolean excluded(ServerLevel level, Rules.Spawns rules, EntityType<?> type) {
        return ghastBlocked(level, type) || Selectors.entity(rules.excluded, type) || Selectors.entity(rules.removed, type);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void potential(LevelEvent.PotentialSpawns e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        Rules.Profile profile = EnvironmentModule.profile(level);
        Rules.Spawns rules = profile != null && profile.spawns.enabled ? profile.spawns : null;
        var current=EventBridge.current(level);
        var theme=SpawnThemes.theme(current,profile);
        if(theme!=null && e.getMobCategory()==net.minecraft.world.entity.MobCategory.MONSTER) {
            for(var entry:List.copyOf(e.getSpawnerDataList()))e.removeSpawnerData(entry);
            for(var entry:SpawnThemes.entries(theme))if(!excluded(level,rules,entry.type))e.addSpawnerData(entry);
            return;
        }
        for (MobSpawnSettings.SpawnerData entry : List.copyOf(e.getSpawnerDataList())) {
            if (profile != null && profile.mechanics.friendlyOnly && e.getMobCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
                e.removeSpawnerData(entry); continue;
            }
            if (ghastBlocked(level, entry.type) || rules != null && (Selectors.entity(rules.excluded, entry.type) || Selectors.entity(rules.removed, entry.type))) {
                e.removeSpawnerData(entry);
                continue;
            }
            if (rules == null) continue;
            boolean howling = current!=null && current.getPath().equals("howling_moon") && entry.type == EntityType.WOLF;
            if (profile.mechanics.wildlifeGroupMultiplier > 1 && e.getMobCategory() == net.minecraft.world.entity.MobCategory.CREATURE) {
                e.removeSpawnerData(entry);
                e.addSpawnerData(new MobSpawnSettings.SpawnerData(entry.type, entry.getWeight().asInt() * (howling ? 3 : 1), entry.minCount,
                    Math.min(8, Math.max(entry.maxCount, (int)Math.ceil(entry.maxCount * profile.mechanics.wildlifeGroupMultiplier)))));
                continue;
            }
            for (Rules.Replacement replacement : rules.replacements) {
                if (!Selectors.entity(List.of(replacement.from), entry.type)) continue;
                EntityType<?> to = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(replacement.to));
                // Cross-category replacement would bypass natural category caps. Reject it.
                if (to.getCategory() != e.getMobCategory()) break;
                e.removeSpawnerData(entry);
                if (!excluded(level, rules, to)) e.addSpawnerData(new MobSpawnSettings.SpawnerData(to, entry.getWeight().asInt(), entry.minCount, entry.maxCount));
                break; // first matching rule wins; no replacement chains
            }
        }
        if (rules != null && e.getMobCategory() == net.minecraft.world.entity.MobCategory.MONSTER && !profile.mechanics.friendlyOnly) {
            var id = current;
            if (id.getNamespace().equals(GameplayPatch.ID)) {
                for (var entry : MOON_SPAWNS.getOrDefault(id.getPath(),List.of()))
                    if (!excluded(level,rules,entry.type)) e.addSpawnerData(entry);
            }
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void placement(MobSpawnEvent.SpawnPlacementCheck event) {
        if (event.getSpawnType() != net.minecraft.world.entity.MobSpawnType.NATURAL
                || event.getResult() != MobSpawnEvent.SpawnPlacementCheck.Result.DEFAULT) return;
        ServerLevel level = event.getLevel().getLevel();
        Rules.Profile profile = EnvironmentModule.profile(level);
        if (profile == null) return;
        EntityType<?> type=event.getEntityType();
        var id = EventBridge.current(level);
        var theme=SpawnThemes.theme(id,profile);
        if(theme!=null && type.getCategory()==net.minecraft.world.entity.MobCategory.MONSTER && !SpawnThemes.allows(theme,type)) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);return;
        }
        if (theme!=null && SpawnThemes.entries(theme).stream().anyMatch(s -> s.type == type)
                && !excluded(level,profile.spawns,type)) {
            // Nether mobs normally use Nether-specific placement predicates. Permit
            // ordinary dark, solid-ground Overworld placement; entity collisions and
            // the normal monster cap are still checked by NaturalSpawner afterward.
            if (level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                    && net.minecraft.world.entity.monster.Monster.isDarkEnoughToSpawn(level,event.getPos(),level.random)
                    && level.getBlockState(event.getPos().below()).isValidSpawn(level,event.getPos().below(),type))
                event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
            else event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
            return;
        }
        if(event.getDefaultResult())return;
        if (profile.mechanics.wildlifeGroupMultiplier <= 1) return;
        if (!List.of(EntityType.COW,EntityType.PIG,EntityType.SHEEP,EntityType.CHICKEN,EntityType.WOLF).contains(type)) return;
        // Keep biome candidates and grass placement; relax only nighttime animal brightness.
        boolean nativeBiome = level.getBiome(event.getPos()).value().getMobSettings()
            .getMobs(net.minecraft.world.entity.MobCategory.CREATURE).unwrap().stream().anyMatch(s -> s.type == type);
        if (nativeBiome && level.getBlockState(event.getPos().below()).is(net.minecraft.tags.BlockTags.ANIMALS_SPAWNABLE_ON)
                && level.isEmptyBlock(event.getPos()) && level.isEmptyBlock(event.getPos().above()))
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void finalizeSpawn(FinalizeSpawnEvent e) {
        ServerLevel level = e.getLevel().getLevel();
        EntityType<?> type = e.getEntity().getType();
        Rules.Profile profile = EnvironmentModule.profile(level);
        Rules.Spawns rules = profile != null && profile.spawns.enabled ? profile.spawns : null;
        var theme=SpawnThemes.theme(EventBridge.current(level),profile);
        if(theme!=null && type.getCategory()==net.minecraft.world.entity.MobCategory.MONSTER
                && SpawnThemes.controlledReason(e.getSpawnType()) && !SpawnThemes.allows(theme,type)) {
            e.setSpawnCancelled(true);return;
        }
        if (profile != null && profile.mechanics.friendlyOnly && type.getCategory() == net.minecraft.world.entity.MobCategory.MONSTER) {
            e.setSpawnCancelled(true); return;
        }
        if (ghastBlocked(level, type) || rules != null && rules.spawnTypes.contains(e.getSpawnType().name()) && Selectors.entity(rules.excluded, type))
            e.setSpawnCancelled(true);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if(event.getLevel() instanceof ServerLevel level && event.getEntity() instanceof net.minecraft.world.entity.Mob mob
                && SpawnThemes.canCull(mob,EnvironmentModule.theme(level)))event.setCanceled(true);
    }
}
