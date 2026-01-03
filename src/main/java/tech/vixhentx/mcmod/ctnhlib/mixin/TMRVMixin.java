package tech.vixhentx.mcmod.ctnhlib.mixin;

import dev.nolij.toomanyrecipeviewers.impl.jei.api.recipe.RecipeManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RecipeManager.Category.Recipe.class, remap = false)
public abstract class TMRVMixin {
    @Shadow
    public abstract @Nullable ResourceLocation getOriginalID();

    @Redirect(method = "getID", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;fromNamespaceAndPath(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    ResourceLocation keepOriginalID(String namespace, String path){
        return getOriginalID();
    }
}
