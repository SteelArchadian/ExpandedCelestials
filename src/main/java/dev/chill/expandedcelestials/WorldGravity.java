package dev.chill.expandedcelestials;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
public final class WorldGravity {
    private WorldGravity() {}
    public static double apply(Entity entity,double original) {
        if(original==0||entity instanceof Player)return original; // Players already use synced attribute modifiers.
        double multiplier=entity.level() instanceof ServerLevel level?EnvironmentModule.gravity(level)
            :entity.level().isClientSide()?ClientEnvironment.gravity(entity.level()):1;
        return scale(original,false,multiplier);
    }
    static double scale(double original,boolean player,double multiplier){return player?original:original*multiplier;}
}
