package tech.vixhentx.mcmod.ctnhlib.common;

import com.gregtechceu.gtceu.data.pack.GTPackSource;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tterrag.registrate.util.entry.ItemEntry;
import tech.vixhentx.mcmod.ctnhlib.command.CTNHCommands;
import tech.vixhentx.mcmod.ctnhlib.data.DataFilterPack;
import tech.vixhentx.mcmod.ctnhlib.jade.GTProvidersRegistrar;
import tech.vixhentx.mcmod.ctnhlib.registrate.CTNHLibNetworking;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CommonProxy {

    public CommonProxy(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        eventBus.register(this);
        // Forge-bus events such as RegisterCommandsEvent must be subscribed to the global Forge bus.
        MinecraftForge.EVENT_BUS.register(CommonProxy.class);
        init();

        ItemEntry<MultiblockHelper> multiblockHelper = REGISTRATE
                .item("mutiblock_helper", MultiblockHelper::new)
                .register();
    }

    public static void init() {
        GTProvidersRegistrar.init();
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CTNHLibNetworking::init);
    }

    @SubscribeEvent
    public void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {

            event.addRepositorySource(new GTPackSource("ctnhlib:filter_data",
                    event.getPackType(),
                    Pack.Position.TOP,
                    DataFilterPack::new));
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CTNHCommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
