package tech.vixhentx.mcmod.ctnhlib.api;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

public interface ICrossParallelRecipeLogicMachine extends IRecipeLogicMachine {

    @Nullable
    Component modifyRecipeAfterMerge(GTRecipe recipe, RecipeHandlerGroup group);
}
