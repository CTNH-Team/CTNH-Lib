package tech.vixhentx.mcmod.ctnhlib.langprovider;

import net.minecraft.network.chat.Component;

public sealed class Lang permits TranslatedLang {
    //inject by processor
    String key;

    Lang() {
    }
    Lang(String key) {
        this.key = key;
    }

    //simplify translation
    public Component translate(Object... args){
        return Component.translatable(key, args);
    }
}
