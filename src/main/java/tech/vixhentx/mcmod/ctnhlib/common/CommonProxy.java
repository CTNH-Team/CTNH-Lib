package tech.vixhentx.mcmod.ctnhlib.common;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.jade.GTProvidersRegistrar;

@Mod.EventBusSubscriber(modid = CTNHLib.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonProxy {
    public CommonProxy(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        eventBus.register(this);
        init();
    }
    public static void init() {
        GTProvidersRegistrar.init();
    }
}
