package tech.vixhentx.mcmod.ctnhlib.registrate.builders;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.world.item.Item;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;

public class CTNHItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {
    protected CTNHItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        super(owner, parent, name, callback, factory);
    }

    public static <T extends Item, P> CTNHItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        return (CTNHItemBuilder<T, P>) new CTNHItemBuilder<>(owner, parent, name, callback, factory)
                .defaultModel().defaultLang();
    }

    public CTNHItemBuilder<T, P> cnlang(String name) {
        this.setData(CNLANG, (ctx, prov) ->
                prov.add(ctx.getEntry().getDescriptionId(), name)
        );
        return this;
    }
}
