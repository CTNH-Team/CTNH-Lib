package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import net.minecraft.resources.ResourceLocation;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;


public class CTNHMaterial extends Material {
    protected CTNHMaterial(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }
    public static class Builder extends Material.Builder{

        public CNRegistrate registrate;
        public Builder(CNRegistrate registrate, ResourceLocation resourceLocation) {
            super(resourceLocation);
            this.registrate = registrate;
        }

        public Builder lang(String lang){
            registrate.addRawLang(id.toLanguageKey("material"), lang);
            return this;
        }

        public Builder cnlang(String lang){
            registrate.addRawCNLang(id.toLanguageKey("material"), lang);
            return this;
        }
    }
}
