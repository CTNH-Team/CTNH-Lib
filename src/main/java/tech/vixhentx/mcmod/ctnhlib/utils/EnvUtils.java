package tech.vixhentx.mcmod.ctnhlib.utils;

import lombok.Getter;
import net.minecraftforge.fml.loading.FMLLoader;

public class EnvUtils {
    static final public boolean isDataGen = FMLLoader.getLaunchHandler().isData();;
}
