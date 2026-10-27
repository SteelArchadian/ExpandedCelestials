package dev.chill.expandedcelestials;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(ExpandedCelestials.MODID)
public final class ExpandedCelestials {
    public static final String MODID = "expandedcelestials";

    public ExpandedCelestials(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, ECConfig.SPEC, "expandedcelestials-common.toml");
        GameplayPatch.install(modBus);
        modBus.addListener(ExpandedCelestials::addMoonPack);
    }

    /** The moons are a built-in, always-on datapack so the extraMoons switch can leave them out entirely. */
    private static void addMoonPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA || !ECConfig.on(ECConfig.EXTRA_MOONS)) return;
        event.addPackFinders(ResourceLocation.fromNamespaceAndPath(MODID, "packs/moons"), PackType.SERVER_DATA,
            Component.literal("Expanded Celestials moons"), PackSource.BUILT_IN, true, Pack.Position.TOP);
    }
}
