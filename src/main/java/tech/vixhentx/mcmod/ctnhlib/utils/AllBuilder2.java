package tech.vixhentx.mcmod.ctnhlib.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonnullType;

public class AllBuilder2<T, P> extends AbstractBuilder<T, T, P, AllBuilder2<T, P>> {

    private final T data;

    public AllBuilder2(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                       ResourceKey<Registry<T>> registryKey, T data) {
        super(owner, parent, name, callback, registryKey);
        this.data = data;
    }

    @Override
    protected @NonnullType T createEntry() {
        return data;
    }
}
