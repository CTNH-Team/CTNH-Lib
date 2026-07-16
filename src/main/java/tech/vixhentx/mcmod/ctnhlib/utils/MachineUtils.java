package tech.vixhentx.mcmod.ctnhlib.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class MachineUtils {

    public static boolean canInputItems(IRecipeLogicMachine machine, ItemStack... items) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputItems(items).buildRuntime(), machine);
    }

    public static boolean canInputItems(RecipeHandlerGroup group, ItemStack... items) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputItems(items).buildRuntime(), group);
    }

    public static boolean canInputItems(IRecipeLogicMachine machine, Item... items) {
        return canInputItems(machine, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean canInputItems(RecipeHandlerGroup group, Item... items) {
        return canInputItems(group, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean canOutputItems(IRecipeLogicMachine machine, ItemStack... items) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputItems(items).buildRuntime(), machine);
    }

    public static boolean canOutputItems(RecipeHandlerGroup group, ItemStack... items) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputItems(items).buildRuntime(), group);
    }

    public static boolean canOutputItems(IRecipeLogicMachine machine, Item... items) {
        return canOutputItems(machine, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean canOutputItems(RecipeHandlerGroup group, Item... items) {
        return canOutputItems(group, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean inputItems(IRecipeLogicMachine machine, ItemStack... items) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputItems(items).buildRuntime(), machine, IO.IN);
    }

    public static boolean inputItems(RecipeHandlerGroup group, ItemStack... items) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputItems(items).buildRuntime(), group, IO.IN);
    }

    public static boolean inputItems(IRecipeLogicMachine machine, Item... items) {
        return inputItems(machine, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean inputItems(RecipeHandlerGroup group, Item... items) {
        return inputItems(group, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean outputItems(IRecipeLogicMachine machine, ItemStack... items) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputItems(items).buildRuntime(), machine, IO.OUT);
    }

    public static boolean outputItems(RecipeHandlerGroup group, ItemStack... items) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputItems(items).buildRuntime(), group, IO.OUT);
    }

    public static boolean outputItems(IRecipeLogicMachine machine, Item... items) {
        return outputItems(machine, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean outputItems(RecipeHandlerGroup group, Item... items) {
        return outputItems(group, Arrays.stream(items).map(ItemStack::new).toArray(ItemStack[]::new));
    }

    public static boolean canInputFluids(IRecipeLogicMachine machine, FluidStack... fluids) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputFluids(fluids).buildRuntime(), machine);
    }

    public static boolean canInputFluids(RecipeHandlerGroup group, FluidStack... fluids) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputFluids(fluids).buildRuntime(), group);
    }

    public static boolean canInputFluids(IRecipeLogicMachine machine, int amount, Fluid... fluids) {
        return canInputFluids(machine, toFluidStacks(amount, fluids));
    }

    public static boolean canInputFluids(RecipeHandlerGroup group, int amount, Fluid... fluids) {
        return canInputFluids(group, toFluidStacks(amount, fluids));
    }

    public static boolean canOutputFluids(IRecipeLogicMachine machine, FluidStack... fluids) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputFluids(fluids).buildRuntime(), machine);
    }

    public static boolean canOutputFluids(RecipeHandlerGroup group, FluidStack... fluids) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputFluids(fluids).buildRuntime(), group);
    }

    public static boolean canOutputFluids(IRecipeLogicMachine machine, int amount, Fluid... fluids) {
        return canOutputFluids(machine, toFluidStacks(amount, fluids));
    }

    public static boolean canOutputFluids(RecipeHandlerGroup group, int amount, Fluid... fluids) {
        return canOutputFluids(group, toFluidStacks(amount, fluids));
    }

    public static boolean inputFluids(IRecipeLogicMachine machine, FluidStack... fluids) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputFluids(fluids).buildRuntime(), machine, IO.IN);
    }

    public static boolean inputFluids(RecipeHandlerGroup group, FluidStack... fluids) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputFluids(fluids).buildRuntime(), group, IO.IN);
    }

    public static boolean inputFluids(IRecipeLogicMachine machine, int amount, Fluid... fluids) {
        return inputFluids(machine, toFluidStacks(amount, fluids));
    }

    public static boolean inputFluids(RecipeHandlerGroup group, int amount, Fluid... fluids) {
        return inputFluids(group, toFluidStacks(amount, fluids));
    }

    public static boolean outputFluids(IRecipeLogicMachine machine, FluidStack... fluids) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputFluids(fluids).buildRuntime(), machine, IO.OUT);
    }

    public static boolean outputFluids(RecipeHandlerGroup group, FluidStack... fluids) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputFluids(fluids).buildRuntime(), group, IO.OUT);
    }

    public static boolean outputFluids(IRecipeLogicMachine machine, int amount, Fluid... fluids) {
        return outputFluids(machine, toFluidStacks(amount, fluids));
    }

    public static boolean outputFluids(RecipeHandlerGroup group, int amount, Fluid... fluids) {
        return outputFluids(group, toFluidStacks(amount, fluids));
    }

    public static boolean canInputEU(IRecipeLogicMachine machine, long eu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputEU(eu).buildRuntime(), machine);
    }

    public static boolean canInputEU(RecipeHandlerGroup group, long eu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputEU(eu).buildRuntime(), group);
    }

    public static boolean canOutputEU(IRecipeLogicMachine machine, long eu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputEU(eu).buildRuntime(), machine);
    }

    public static boolean canOutputEU(RecipeHandlerGroup group, long eu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputEU(eu).buildRuntime(), group);
    }

    public static boolean inputEU(IRecipeLogicMachine machine, long eu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputEU(eu).buildRuntime(), machine, IO.IN);
    }

    public static boolean inputEU(RecipeHandlerGroup group, long eu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputEU(eu).buildRuntime(), group, IO.IN);
    }

    public static boolean outputEU(IRecipeLogicMachine machine, long eu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputEU(eu).buildRuntime(), machine, IO.OUT);
    }

    public static boolean outputEU(RecipeHandlerGroup group, long eu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputEU(eu).buildRuntime(), group, IO.OUT);
    }

    public static boolean canInputCWU(IRecipeLogicMachine machine, int cwu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputCWU(cwu).buildRuntime(), machine);
    }

    public static boolean canInputCWU(RecipeHandlerGroup group, int cwu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().inputCWU(cwu).buildRuntime(), group);
    }

    public static boolean canOutputCWU(IRecipeLogicMachine machine, int cwu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputCWU(cwu).buildRuntime(), machine);
    }

    public static boolean canOutputCWU(RecipeHandlerGroup group, int cwu) {
        return canExecuteRecipe(GTRecipeBuilder.ofRaw().outputCWU(cwu).buildRuntime(), group);
    }

    public static boolean inputCWU(IRecipeLogicMachine machine, int cwu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputCWU(cwu).buildRuntime(), machine, IO.IN);
    }

    public static boolean inputCWU(RecipeHandlerGroup group, int cwu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().inputCWU(cwu).buildRuntime(), group, IO.IN);
    }

    public static boolean outputCWU(IRecipeLogicMachine machine, int cwu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputCWU(cwu).buildRuntime(), machine, IO.OUT);
    }

    public static boolean outputCWU(RecipeHandlerGroup group, int cwu) {
        return executeRecipe(GTRecipeBuilder.ofRaw().outputCWU(cwu).buildRuntime(), group, IO.OUT);
    }

    private static FluidStack[] toFluidStacks(int amount, Fluid... fluids) {
        return Arrays.stream(fluids).map(fluid -> new FluidStack(fluid, amount)).toArray(FluidStack[]::new);
    }

    private static boolean canExecuteRecipe(GTRecipe recipe, RecipeHandlerGroup group) {
        return RecipeHelper.matchRecipe(group, recipe).isSuccess();
    }

    private static boolean canExecuteRecipe(GTRecipe recipe, IRecipeLogicMachine machine) {
        return machine.getRecipeHandlerGroups().stream().anyMatch(group -> canExecuteRecipe(recipe, group));
    }

    public static boolean executeRecipe(GTRecipe recipe, RecipeHandlerGroup group, IO io) {
        if(RecipeHelper.matchRecipe(group, recipe).isSuccess()) {
            RecipeHelper.handleRecipeIO(group, recipe, io);
            return true;
        }
        return false;
    }

    public static boolean executeRecipe(GTRecipe recipe, IRecipeLogicMachine machine, IO io) {
        for(var group: machine.getRecipeHandlerGroups()) {
            if(RecipeHelper.matchRecipe(group, recipe).isSuccess()) {
                RecipeHelper.handleRecipeIO(group, recipe, io);
                return true;
            }
        }
        return false;
    }

    public static BlockPos getOffset(MetaMachine machine, int leftoff, int upoff, int backoff) {
        var pos = machine.getPos();
        var facing = machine.getFrontFacing();
        switch (facing) {
            case NORTH -> {
                return pos.offset(-leftoff, upoff, backoff);
            }
            case SOUTH -> {
                return pos.offset(leftoff, upoff, -backoff);
            }
            case WEST -> {
                return pos.offset(backoff, upoff, -leftoff);
            }
            case EAST -> {
                return pos.offset(-backoff, upoff, leftoff);
            }
        }
        return pos;
    }

    public static AABB getArea(MetaMachine machine, int left1, int up1, int back1, int left2, int up2, int back2) {
        var pos = machine.getPos();
        var facing = machine.getFrontFacing();
        switch (facing) {
            case NORTH -> {
                return AABB.of(BoundingBox.fromCorners(pos.offset(-left1, up1, back1), pos.offset(-left2, up2, back2)));
            }
            case SOUTH -> {
                return AABB.of(BoundingBox.fromCorners(pos.offset(left1, up1, -back1), pos.offset(left2, up2, -back2)));
            }
            case WEST -> {
                return AABB.of(BoundingBox.fromCorners(pos.offset(back1, up1, -left1), pos.offset(back2, up2, -left2)));
            }
            case EAST -> {
                return AABB.of(BoundingBox.fromCorners(pos.offset(-back1, up1, left1), pos.offset(-back2, up2, left2)));
            }
        }
        return AABB.of(BoundingBox.fromCorners(pos, pos));
    }
}
