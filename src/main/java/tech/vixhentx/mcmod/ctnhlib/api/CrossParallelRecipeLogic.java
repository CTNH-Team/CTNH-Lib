package tech.vixhentx.mcmod.ctnhlib.api;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;

import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

import static com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList.UNDYED;

public class CrossParallelRecipeLogic extends RecipeLogic {

    public static final int MAX_MERGED = 64;

    public GTRecipe mergedRecipe;
    public Set<ResourceLocation> recipeIDs = new HashSet<>();

    public CrossParallelRecipeLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    @Override
    public void findAndHandleRecipe() {
        failureReasonsMap.clear();
        recipeDirty = false;
        lastRecipe = null;
        lastOriginRecipe = null;
        for (var group : machine.getRecipeHandlerGroups()) {
            int merged = 0;
            var iterator = machine.getRecipeType().getRecipeIterator(group,
                    r -> tyrMergeMatchedRecipe(r, group));
            while (iterator != null && iterator.hasNext() && merged <= MAX_MERGED) {
                iterator.next();
                merged++;
            }

            if (merged == 0 && !group.isEmpty()) {
                for (var logic : machine.getRecipeType().getCustomRecipeLogicRunners()) {
                    var recipe = logic.createCustomRecipe(group);
                    if (recipe != null && tyrMergeMatchedRecipe(recipe, group)) {
                        merged++;
                    }
                }
            }

            if (merged != 0) {
                setupMergedRecipe();
                break;
            }
        }
    }

    public boolean tyrMergeMatchedRecipe(GTRecipeDefinition match, RecipeHandlerGroup group) {
        if (recipeIDs.contains(match.id)) {
            return false;
        }
        var modified = match.toRuntime();
        var failReason = machine.modifyRecipe(modified, group);
        if (failReason == null) {
            var recipeMatch = checkRecipe(modified, group);
            if (recipeMatch.isSuccess()) {
                lastGroup = group;
                if (group.getColor() != UNDYED) lastGroupColor = group.getColor();
                failReason = machine.beforeWorking(modified);
                if (failReason != null) {
                    failureReasonsMap.put(modified.id, failReason);
                    return false;
                }
                var handledIO = handleRecipeIO(modified, IO.IN);
                if (handledIO.isSuccess()) {
                    mergeRecipe(modified);
                    return true;
                }
            } else {
                failureReasonsMap.put(match.id, recipeMatch.reason());
            }
        } else {
            failureReasonsMap.put(match.id, failReason);
        }
        return false;
    }

    public void setupMergedRecipe() {
        if (machine instanceof ICrossParallelRecipeLogicMachine c) {
            var failReason = c.modifyRecipeAfterMerge(mergedRecipe, getLastGroup());
            if (failReason != null) {
                failureReasonsMap.put(mergedRecipe.id, failReason);
                mergedRecipe = null;
            }
        }

        if (mergedRecipe != null) {
            failureReasonsMap.clear();
            recipeDirty = true;
            lastRecipe = mergedRecipe;
            setStatus(Status.WORKING);
            progress = 0;
            duration = lastRecipe.duration;
        } else {
            setStatus(Status.IDLE);
            progress = 0;
            duration = 0;
        }
        mergedRecipe = null;
        recipeIDs.clear();
    }

    public void mergeRecipe(GTRecipe toMerge) {
        recipeIDs.add(toMerge.id);

        if (mergedRecipe == null) {
            mergedRecipe = toMerge;
            mergedRecipe.id = GTCEu.id("merged/" + toMerge.recipeCategory.name + "/" + GTValues.RNG.nextLong());
        } else {
            mergedRecipe.inputs.appendAll(toMerge.inputs);
            mergedRecipe.outputs.appendAll(toMerge.outputs);
            mergedRecipe.tickInputs.appendAll(toMerge.tickInputs);
            mergedRecipe.tickOutputs.appendAll(toMerge.tickOutputs);
            mergedRecipe.data.merge(toMerge.data);
            mergedRecipe.tier = Math.max(mergedRecipe.tier, toMerge.tier);
            mergedRecipe.duration = Math.max(mergedRecipe.duration, toMerge.duration);
            mergedRecipe.parallels += toMerge.parallels;
        }
    }
}
