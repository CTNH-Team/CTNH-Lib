package tech.vixhentx.mcmod.ctnhlib.langprovider;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public final class TranslatedLang extends Lang{

    public final String translation;

    public TranslatedLang(String translation) {
        this.translation = translation;
    }
    public static TranslatedLang of(String translation) {
        return new TranslatedLang(translation);
    }
    public Pair<String,String> pair() {
        return Pair.of(key, translation);
    }


    Lang erase() {
        return new Lang(key);
    }
}
