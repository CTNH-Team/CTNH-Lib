package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import tech.vixhentx.mcmod.ctnhlib.registrate.CNRegistrate;

public class CTNHRecipeType extends GTRecipeType {

    public CNRegistrate registrate;

    public CTNHRecipeType(CNRegistrate registrate, ResourceLocation registryName, String group,
                          RecipeType<?>... proxyRecipes) {
        super(registryName, group, proxyRecipes);
        this.registrate = registrate;
    }

    public CTNHRecipeType lang(String lang) {
        registrate.addRawLang(registryName.toLanguageKey(), lang);
        return this;
    }

    public CTNHRecipeType cnlang(String lang) {
        registrate.addRawCNLang(registryName.toLanguageKey(), lang);
        return this;
    }
}
