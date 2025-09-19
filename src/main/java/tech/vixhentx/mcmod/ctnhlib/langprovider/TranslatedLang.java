package tech.vixhentx.mcmod.ctnhlib.langprovider;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public final class TranslatedLang extends Lang{

    public final String en_translation, cn_translation;

    public TranslatedLang(String en_translation, String cn_translation) {
        this.en_translation = en_translation;
        this.cn_translation = cn_translation;
    }

    Lang erase() {
        return new Lang(key);
    }
}
