package tech.vixhentx.mcmod.ctnhlib.mixin;

import com.gregtechceu.gtceu.common.data.GTRecipes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tech.vixhentx.mcmod.ctnhlib.data.DataFilterPack;

@Mixin(value = GTRecipes.class, remap = false)
public class GTRecipesMixin {
    @Inject(method = "recipeRemoval", at = @At("HEAD"))
    private static void injectRemoval(CallbackInfo ci){
        DataFilterPack.FILTERED.clear();
    }
}
