package dev.chill.expandedcelestials;

import net.minecraft.commands.CommandSourceStack;

/** Moon controls belong to the local world owner, or to server operators. */
public final class CommandAccess {
    private CommandAccess() {}
    public static boolean canControl(CommandSourceStack source) {
        if (source.hasPermission(2)) return true;
        var player = source.getPlayer();
        if (player == null) return false;
        var server = source.getServer();
        boolean singleplayer = server.isSingleplayer();
        boolean owner = singleplayer && server.isSingleplayerOwner(player.getGameProfile());
        return allowed(false, singleplayer, owner);
    }
    static boolean allowed(boolean operator, boolean singleplayer, boolean owner) {
        return operator || singleplayer && owner;
    }
}
