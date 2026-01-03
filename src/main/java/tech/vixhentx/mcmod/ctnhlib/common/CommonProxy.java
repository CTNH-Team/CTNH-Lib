package tech.vixhentx.mcmod.ctnhlib.common;

import com.gregtechceu.gtceu.data.pack.GTPackSource;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tech.vixhentx.mcmod.ctnhlib.data.DataFilterPack;
import tech.vixhentx.mcmod.ctnhlib.jade.GTProvidersRegistrar;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class CommonProxy {
    public CommonProxy(FMLJavaModLoadingContext context) {
        IEventBus eventBus = context.getModEventBus();
        eventBus.register(this);
        init();

        ItemEntry<MultiblockHelper> multiblockHelper = REGISTRATE
                .item("mutiblock_helper", MultiblockHelper::new)
                .register();
    }
    public static void init() {
        GTProvidersRegistrar.init();
    }

    @SubscribeEvent
    public void registerPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA){

            event.addRepositorySource(new GTPackSource("ctnhlib:filter_data",
                    event.getPackType(),
                    Pack.Position.TOP,
                    DataFilterPack::new));
        }
    }
}
