package dev.chill.expandedcelestials;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** One cached environment per level; no forecast lookup inside individual gravity calls. */
public final class EnvironmentModule {
    private record State(EnvironmentPayload payload,SpawnThemes.Theme theme,Rules.Profile profile) {}
    private static final Map<ServerLevel,State> STATES=new ConcurrentHashMap<>();
    private final Map<UUID,EnvironmentPayload> sent=new ConcurrentHashMap<>();
    private final Map<ServerLevel,ArrayDeque<Mob>> cleanup=new ConcurrentHashMap<>();
    public static double gravity(ServerLevel level) {var state=STATES.get(level);return state==null?1:state.payload.gravity();}
    public static SpawnThemes.Theme theme(ServerLevel level) {var state=STATES.get(level);return state==null?null:state.theme;}
    public static Rules.Profile profile(ServerLevel level){var state=STATES.get(level);return state==null?EventBridge.profile(level):state.profile;}
    @SubscribeEvent public void tick(LevelTickEvent.Pre event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;
        var profile=EventBridge.profile(level);
        var theme=SpawnThemes.theme(EventBridge.current(level),profile);
        var mechanics=profile==null?null:profile.mechanics;
        double gravity=mechanics!=null&&mechanics.worldGravity?profile.gravityMultiplier:1;
        var payload=new EnvironmentPayload(level.dimension().location(),gravity,
            mechanics!=null&&mechanics.enderParticles,mechanics!=null&&mechanics.enderSky,
            mechanics==null?0:mechanics.enderParticleRate);
        var state=new State(payload,theme,profile);var previous=STATES.put(level,state);
        if(theme!=null && (previous==null||previous.theme!=theme)) {
            var pending=new ArrayDeque<Mob>();
            // Copy candidates before discarding; entity storage is not mutated during iteration.
            for(var entity:level.getAllEntities())if(entity instanceof Mob mob && SpawnThemes.canCull(mob,theme))pending.add(mob);
            cleanup.put(level,pending);
        } else if(theme==null)cleanup.remove(level);
        var pending=cleanup.get(level);
        if(pending!=null)for(int i=0;i<12&&!pending.isEmpty();i++) {
            var mob=pending.removeFirst();if(!mob.isRemoved()&&SpawnThemes.canCull(mob,theme))mob.discard();
        }
        for(var player:level.players()) {
            if(!payload.equals(sent.get(player.getUUID())) || level.getGameTime()%100==0) {
                PacketDistributor.sendToPlayer(player,payload);sent.put(player.getUUID(),payload);
            }
        }
    }
    @SubscribeEvent public void unload(LevelEvent.Unload event) {
        if(event.getLevel() instanceof ServerLevel level){STATES.remove(level);cleanup.remove(level);}
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event){sent.remove(event.getEntity().getUUID());}
    @SubscribeEvent public void stop(ServerStoppedEvent event){STATES.clear();cleanup.clear();sent.clear();}
}
