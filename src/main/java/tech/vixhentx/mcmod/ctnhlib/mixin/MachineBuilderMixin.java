package tech.vixhentx.mcmod.ctnhlib.mixin;

import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tech.vixhentx.mcmod.ctnhlib.registrate.builders.ICNBuilder;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

@Mixin(value = MachineBuilder.class, remap = false)
public abstract class MachineBuilderMixin {

    @Redirect(
            method = "register()Lcom/gregtechceu/gtceu/api/machine/MachineDefinition;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/builders/BlockBuilder;register()Lcom/tterrag/registrate/util/entry/BlockEntry;"
            )
    )
    private BlockEntry<?> redirectBlockBuilderRegister(BlockBuilder<Block, ? extends AbstractRegistrate<?>> builder) {

        if (this instanceof ICNBuilder cnBuilder && cnBuilder.getCNLangValue() != null) {
            builder.setData(CNLANG, (ctx, prov) ->
                    prov.add(ctx.getEntry().getDescriptionId(), cnBuilder.getCNLangValue()));
        }
        return builder.register();
    }
}
