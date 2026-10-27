package dev.chill.expandedcelestials;

import net.minecraft.server.level.ServerPlayer;

/** Per-player lifecycle. A late join starts effects just like a lunar transition. */
public interface PlayerMechanic {
    default void start(ServerPlayer player, Rules.Profile profile) {}
    default void tick(ServerPlayer player, Rules.Profile profile) {}
    default void end(ServerPlayer player) {}
}
