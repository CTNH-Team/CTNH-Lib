package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.gregtechceu.gtceu.api.registry.registrate.GTBlockBuilder;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

public class CTNHBlockBuilder<T extends Block, P> extends GTBlockBuilder<T , P> {
    protected CTNHBlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, factory, initialProperties);
    }

    public static <T extends Block, P> CTNHBlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name,
                                                                   BuilderCallback callback,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (CTNHBlockBuilder<T, P>) new CTNHBlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
                .defaultBlockstate().defaultLoot().defaultLang();
    }

    public CTNHBlockBuilder<T, P> cnlang(String name) {
        this.setData(CNLANG, (ctx, prov) ->
                prov.add(ctx.getEntry().getDescriptionId(), name)
        );
        return this;
    }
}
