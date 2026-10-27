package dev.chill.expandedcelestials;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

final class EffectModule implements PlayerMechanic {
    @Override public void tick(ServerPlayer player, Rules.Profile profile) {
        if (profile != null && profile.mechanics.hasteLevel > 0 && player.tickCount % 10 == 0) {
            // Short renewal expires naturally, preserving potions/beacons from other sources.
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30,
                profile.mechanics.hasteLevel - 1, true, false, true));
        }
    }
}
