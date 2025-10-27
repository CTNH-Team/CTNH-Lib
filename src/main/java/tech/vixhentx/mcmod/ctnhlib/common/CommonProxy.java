package tech.vixhentx.mcmod.ctnhlib.common;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.jade.GTProvidersRegistrar;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

@Mod.EventBusSubscriber(modid = CTNHLib.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonProxy {
    public CommonProxy(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        eventBus.register(this);
        init();

        ItemEntry<MultiblockHelper> multiblockHelper = REGISTRATE
                .item("mutiblock_helper", MultiblockHelper::new)
                .lang("mutiblock_helper")
                .register();
    }
    public static void init() {
        GTProvidersRegistrar.init();
    }
}
