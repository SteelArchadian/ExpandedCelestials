package dev.chill.expandedcelestials;

import java.nio.file.Path;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExpandedCelestials.MODID)
public final class ExpandedCelestials {
    public static final String MODID = "expandedcelestials";

    public ExpandedCelestials() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ECConfig.SPEC, "expandedcelestials-common.toml");
        GameplayPatch.install(modBus);
        modBus.addListener(ExpandedCelestials::addMoonPack);
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(MoonNetwork::register));
    }

    /** The moons are a built-in, always-on datapack so the extraMoons switch can leave them out entirely. */
    private static void addMoonPack(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA || !ECConfig.on(ECConfig.EXTRA_MOONS)) return;
        Path root = ModList.get().getModFileById(MODID).getFile().findResource("packs/moons");
        Pack pack = Pack.readMetaAndCreate("mod/" + MODID + ":packs/moons", Component.literal("Expanded Celestials moons"), true,
            name -> new PathPackResources(name, root, true), PackType.SERVER_DATA, Pack.Position.TOP, PackSource.BUILT_IN);
        if (pack != null) event.addRepositorySource(consumer -> consumer.accept(pack));
    }
}
