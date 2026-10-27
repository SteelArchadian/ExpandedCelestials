package dev.chill.expandedcelestials;

import com.mojang.brigadier.Command;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

public final class GameplayController {
    private static final class Session {
        final List<PlayerMechanic> modules = List.of(new AttributeModule(), new EffectModule());
        ResourceLocation event;
        ResourceLocation dimension;
        long revision = -1;
    }
    private final Map<UUID, Session> sessions = new java.util.concurrent.ConcurrentHashMap<>();
    @SubscribeEvent public void starting(ServerStartingEvent event) {
        sessions.clear();
        try { RuleStore.reload(); }
        catch (Exception error) { throw new IllegalStateException("Invalid " + RuleStore.path() + ": " + error.getMessage(), error); }
    }
    @SubscribeEvent public void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) reconcile(player);
    }
    private void reconcile(ServerPlayer player) {
        if (!player.isAlive() || player.isRemoved()) { cleanup(player); return; }
        Session s = sessions.get(player.getUUID());
        if (s == null) {
            s = new Session();
            for (PlayerMechanic module : s.modules) module.end(player);
            sessions.put(player.getUUID(), s);
        }
        ResourceLocation current = EventBridge.current(player.serverLevel());
        ResourceLocation dimension = player.serverLevel().dimension().location();
        Rules.Profile profile = EventBridge.profile(player.serverLevel());
        if (!Objects.equals(current, s.event) || !Objects.equals(dimension, s.dimension) || s.revision != RuleStore.revision()) {
            for (PlayerMechanic module : s.modules) module.end(player);
            s.event = current; s.dimension = dimension; s.revision = RuleStore.revision();
            if (profile != null) for (PlayerMechanic module : s.modules) module.start(player, profile);
        }
        for (PlayerMechanic module : s.modules) module.tick(player, profile);
    }
    private void cleanup(ServerPlayer player) {
        Session s = sessions.remove(player.getUUID());
        if (s != null) for (PlayerMechanic module : s.modules) module.end(player);
        else new AttributeModule().end(player);
    }
    @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e) { if (e.getEntity() instanceof ServerPlayer p) { cleanup(p); reconcile(p); } }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e) { if (e.getEntity() instanceof ServerPlayer p) cleanup(p); }
    @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent e) { if (e.getEntity() instanceof ServerPlayer p) { cleanup(p); reconcile(p); } }
    @SubscribeEvent public void respawn(PlayerEvent.PlayerRespawnEvent e) { if (e.getEntity() instanceof ServerPlayer p) { cleanup(p); reconcile(p); } }
    @SubscribeEvent public void clone(PlayerEvent.Clone e) {
        if (e.getOriginal() instanceof ServerPlayer old) {
            cleanup(old);
        }
        if (e.getEntity() instanceof ServerPlayer p) cleanup(p);
    }
    @SubscribeEvent public void death(LivingDeathEvent e) { if (e.getEntity() instanceof ServerPlayer p) cleanup(p); }
    @SubscribeEvent public void stopping(ServerStoppingEvent e) { e.getServer().getPlayerList().getPlayers().forEach(this::cleanup); sessions.clear(); }
    @SubscribeEvent public void stopped(ServerStoppedEvent e) { sessions.clear(); }
    @SubscribeEvent public void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("ecplus")
            .then(Commands.literal("status").executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                var forecast = dev.corgitaco.enhancedcelestials2core.EnhancedCelestials.lunarForecastWorldData(player.serverLevel());
                Rules.Profile profile = EventBridge.profile(player.serverLevel());
                String text = "Expanded Celestials: event=" + EventBridge.current(player.serverLevel())
                    + ", active=" + forecast.map(f -> f.isEventActive()).orElse(false)
                    + ", profile=" + (profile != null)
                    + ", gravity=" + player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.GRAVITY)
                    + ", mining=" + player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_BREAK_SPEED)
                    + ", haste=" + (profile == null ? 0 : profile.mechanics.hasteLevel)
                    + ", hastePresent=" + player.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)
                    + ", luck=" + player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.LUCK)
                    + ", worldGravity=" + EnvironmentModule.gravity(player.serverLevel())
                    + ", spawnTheme=" + EnvironmentModule.theme(player.serverLevel())
                    + ", dimension=" + player.serverLevel().dimension().location();
                context.getSource().sendSuccess(() -> Component.literal(text), false);
                return 1;
            }))
            .then(Commands.literal("test").requires(CommandAccess::canControl).then(Commands.argument("moon", com.mojang.brigadier.arguments.StringArgumentType.word())
                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(MoonNames.COMMAND_NAMES, builder))
                .executes(context -> {
                    var level = context.getSource().getLevel();
                    String name = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "moon");
                    var id = MoonNames.id(name);
                    var registry = level.registryAccess().registryOrThrow(dev.corgitaco.enhancedcelestials2core.api.EnhancedCelestialsRegistry.LUNAR_EVENT_KEY);
                    var holder = registry.getHolder(net.minecraft.resources.ResourceKey.create(dev.corgitaco.enhancedcelestials2core.api.EnhancedCelestialsRegistry.LUNAR_EVENT_KEY, id));
                    var forecast = dev.corgitaco.enhancedcelestials2core.EnhancedCelestials.lunarForecastWorldData(level);
                    if (holder.isEmpty() || forecast.isEmpty()) { context.getSource().sendFailure(Component.literal("Moon unavailable: " + id)); return 0; }
                    long time = level.getDayTime();
                    level.setDayTime(time + Math.floorMod(18000L - time, 24000L));
                    forecast.get().setLunarEventTonight(holder.get());
                    // Finish EC2's fade even when doDaylightCycle is disabled.
                    level.setDayTime(level.getDayTime() + dev.corgitaco.enhancedcelestials2core.api.lunarevent.LunarForecast.EVENT_FADE_LENGTH);
                    context.getSource().sendSuccess(() -> Component.literal("Testing " + id + "; time advanced to midnight. Run /ecplus status after a few ticks."), false);
                    return 1;
                })))
            .then(Commands.literal("refreshForecast").requires(CommandAccess::canControl).executes(context -> {
                var forecast = dev.corgitaco.enhancedcelestials2core.EnhancedCelestials.lunarForecastWorldData(context.getSource().getLevel());
                if (forecast.isEmpty()) return 0;
                forecast.get().recomputeForecast();
                context.getSource().sendSuccess(() -> Component.literal("Forecast regenerated using the installed moons. Previous scheduled and forced events were replaced."), false);
                return 1;
            }))
            .then(Commands.literal("reload").requires(CommandAccess::canControl).executes(context -> {
                try {
                    RuleStore.reload();
                    context.getSource().getServer().getPlayerList().getPlayers().forEach(this::reconcile);
                    context.getSource().sendSuccess(() -> Component.literal("Expanded Celestials rules reloaded."), true);
                    return Command.SINGLE_SUCCESS;
                } catch (Exception error) {
                    context.getSource().sendFailure(Component.literal("Rules rejected; previous rules remain active: " + error.getMessage()));
                    GameplayPatch.LOGGER.warn("Rules reload rejected", error);
                    return 0;
                }
            })));
    }
}
