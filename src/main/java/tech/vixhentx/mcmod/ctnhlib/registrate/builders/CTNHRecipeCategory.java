package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CTNHRecipeCategory extends GTRecipeCategory {

    public CNRegistrate registrate;

    public CTNHRecipeCategory(CNRegistrate registrate, @NotNull ResourceLocation registryKey,
                              @NotNull GTRecipeType recipeType) {
        super(registryKey, recipeType);
        this.registrate = registrate;
    }

    public CTNHRecipeCategory lang(String lang) {
        registrate.addRawLang(getLanguageKey(), lang);
        return this;
    }

    public CTNHRecipeCategory cnlang(String lang) {
        registrate.addRawCNLang(getLanguageKey(), lang);
        return this;
    }
}
