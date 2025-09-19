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

    //port to translated lang
    public static TranslatedLang of(String en, String cn){
        return new TranslatedLang(en, cn);
    }
    public static TranslatedLang[] of(String[] en, String[] cn){
        if(en.length!= cn.length){
            LangProcessUtils.warnForIncorrectCount();
        }
        int len = Math.min(en.length, cn.length);
        TranslatedLang[] langs = new TranslatedLang[len];
        for(int i = 0; i < len; i++){
            langs[i] = new TranslatedLang(en[i], cn[i]);
        }
        return langs;
    }
    public static TranslatedLang[] of(String[][] enCn){
        return of(enCn[0], enCn[1]);
    }
    public static TranslatedLang[] of(String... enCn){
        int n = enCn.length;
        if(n % 2!= 0) {
            LangProcessUtils.warnForIncorrectCount();
        }
        n/=2;
        TranslatedLang[] langs = new TranslatedLang[n];
        for(int i = 0; i < n; i++){
            langs[i] = new TranslatedLang(enCn[i*2], enCn[i*2+1]);
        }
        return langs;
    }
}
