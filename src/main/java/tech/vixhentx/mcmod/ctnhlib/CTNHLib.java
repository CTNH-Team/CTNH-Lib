package tech.vixhentx.mcmod.ctnhlib;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import tech.vixhentx.mcmod.ctnhlib.client.ClientProxy;
import tech.vixhentx.mcmod.ctnhlib.common.CommonProxy;

@Mod(CTNHLib.MODID)
public class CTNHLib {
    public static final String MODID = "ctnhlib";

    public static final Logger LOGGER = LogUtils.getLogger();

    public CTNHLib() {
        final var context = FMLJavaModLoadingContext.get();
        //noinspection InstantiationOfUtilityClass
        DistExecutor.unsafeRunForDist(() -> () -> new ClientProxy(context), () -> () -> new CommonProxy(context));

    }


}
