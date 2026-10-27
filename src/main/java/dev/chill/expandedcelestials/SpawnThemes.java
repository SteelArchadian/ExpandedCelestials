package dev.chill.expandedcelestials;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.MobSpawnSettings;

public final class SpawnThemes {
    public enum Theme { NETHER, PILLAGER, ENDER }
    private static MobSpawnSettings.SpawnerData entry(EntityType<?> type,int weight,int min,int max) {
        return new MobSpawnSettings.SpawnerData(type,weight,min,max);
    }
    public static List<MobSpawnSettings.SpawnerData> entries(Theme theme) {
        return switch(theme) {
            case NETHER -> NETHER;
            case PILLAGER -> PILLAGER;
            case ENDER -> ENDER;
        };
    }
    private static final List<MobSpawnSettings.SpawnerData> NETHER=List.of(
        entry(EntityType.WITHER_SKELETON,25,1,2),entry(EntityType.PIGLIN,25,1,2),
        entry(EntityType.ZOMBIFIED_PIGLIN,15,1,2),entry(EntityType.BLAZE,15,1,1),
        entry(EntityType.MAGMA_CUBE,12,1,1),entry(EntityType.HOGLIN,8,1,1));
    private static final List<MobSpawnSettings.SpawnerData> PILLAGER=List.of(
        entry(EntityType.PILLAGER,80,1,2),entry(EntityType.VINDICATOR,18,1,1),entry(EntityType.EVOKER,2,1,1));
    private static final List<MobSpawnSettings.SpawnerData> ENDER=List.of(
        entry(EntityType.ENDERMAN,84,1,2),entry(EntityType.ENDERMITE,16,1,1));
    public static Theme theme(ResourceLocation id,Rules.Profile profile) {
        if(profile==null || !profile.spawns.enabled || !profile.mechanics.themedSpawns || id==null
                || !id.getNamespace().equals(GameplayPatch.ID))return null;
        return switch(id.getPath()){case "eclipse"->Theme.NETHER;case "grey_moon"->Theme.PILLAGER;case "ender_moon"->Theme.ENDER;default->null;};
    }
    public static boolean allows(Theme theme,EntityType<?> type) {
        if(theme==null)return true;
        // Allow the themed mobs' own offspring/conversions, without adding them to the table.
        return entries(theme).stream().anyMatch(e->e.type==type)
            || theme==Theme.NETHER && type==EntityType.ZOGLIN
            || theme==Theme.PILLAGER && type==EntityType.VEX;
    }
    public static boolean controlledReason(MobSpawnType reason) {
        return reason==MobSpawnType.NATURAL || reason==MobSpawnType.CHUNK_GENERATION
            || reason==MobSpawnType.REINFORCEMENT || reason==MobSpawnType.PATROL || reason==MobSpawnType.SPAWNER;
    }
    public static boolean canCull(Mob mob,Theme theme) {
        return theme!=null && mob.getType().getCategory()==MobCategory.MONSTER && !allows(theme,mob.getType())
            && unprotectedWild(mob.getSpawnType(),mob.isPersistenceRequired(),mob.requiresCustomPersistence(),mob.hasCustomName(),mob.isPassenger(),mob.isVehicle());
    }
    static boolean unprotectedWild(MobSpawnType reason,boolean persistent,boolean customPersistent,boolean named,boolean passenger,boolean vehicle) {
        return (reason==MobSpawnType.NATURAL||reason==MobSpawnType.CHUNK_GENERATION)
            && !persistent && !customPersistent && !named && !passenger && !vehicle;
    }
}
