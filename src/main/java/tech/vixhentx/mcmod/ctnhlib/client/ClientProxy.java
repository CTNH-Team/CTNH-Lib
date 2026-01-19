package tech.vixhentx.mcmod.ctnhlib.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.client.render.highlight.HighlightRender;
import tech.vixhentx.mcmod.ctnhlib.common.CommonProxy;

@Mod.EventBusSubscriber(modid = CTNHLib.MODID,bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public ClientProxy(FMLJavaModLoadingContext context) {
        super(context);
        init();
    }
    public static void init() {

    }
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // 调用HighlightRender的hook方法，执行高亮渲染逻辑
        HighlightRender.hook(event);
    }
}
