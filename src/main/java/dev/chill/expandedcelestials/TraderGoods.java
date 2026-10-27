package dev.chill.expandedcelestials;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

public final class TraderGoods {
    private static final Item[] GOODS = {Items.SPYGLASS, Items.NAUTILUS_SHELL, Items.AMETHYST_SHARD, Items.COMPASS, Items.GLOW_BERRIES};
    public static void add(WanderingTrader trader) {
        if (!(trader.level() instanceof ServerLevel level)) return;
        Rules.Profile profile = EventBridge.profile(level);
        if (profile == null || !profile.mechanics.extraTraderGoods) return;
        int first = level.random.nextInt(GOODS.length), second = (first+1+level.random.nextInt(GOODS.length-1))%GOODS.length;
        for (int index : new int[]{first,second}) trader.getOffers().add(new MerchantOffer(
            new ItemCost(Items.EMERALD, index == 1 ? 8 : 4),new ItemStack(GOODS[index]),2,1,.05f));
    }
}
