package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CTNHTagPrefix extends TagPrefix {

    public CNRegistrate registrate;

    public CTNHTagPrefix(CNRegistrate registrate, String name) {
        super(name);
        this.registrate = registrate;
    }

    public CTNHTagPrefix cnlang(String lang) {
        registrate.addRawCNLang(getUnlocalizedName(), lang);
        return this;
    }

    public CTNHTagPrefix lang(String lang) {
        langValue(lang);
        return this;
    }
}
