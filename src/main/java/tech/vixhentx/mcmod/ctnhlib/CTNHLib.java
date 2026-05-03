package tech.vixhentx.mcmod.ctnhlib;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import tech.vixhentx.mcmod.ctnhlib.client.ClientProxy;
import tech.vixhentx.mcmod.ctnhlib.common.CommonProxy;

@Mod(CTNHLib.MODID)
@SuppressWarnings("removal")
public class CTNHLib {

    public static final String MODID = "ctnhlib";

    public static final Logger LOGGER = LogUtils.getLogger();

    public CTNHLib() {
        final var context = FMLJavaModLoadingContext.get();
        DistExecutor.unsafeRunForDist(() -> () -> new ClientProxy(context), () -> () -> new CommonProxy(context));
    }
}
