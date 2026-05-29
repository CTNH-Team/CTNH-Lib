package tech.vixhentx.mcmod.ctnhlib.langprovider;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.ctnhlang.LangFactory;

@LangFactory
public final class Lang implements com.ctnhlang.Lang {

    private final String key;

    private Lang(String key) {
        this.key = key;
    }

    public static Lang genLang(String key) {
        return new Lang(key);
    }

    public static Lang[] genLangArray(String[] keys) {
        Lang[] langs = new Lang[keys.length];
        for (int i = 0; i < keys.length; i++) {
            langs[i] = genLang(keys[i]);
        }
        return langs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public MutableComponent translate(Object... args) {
        return Component.translatable(key, args);
    }

    @Override
    public String key() {
        return key;
    }
}
