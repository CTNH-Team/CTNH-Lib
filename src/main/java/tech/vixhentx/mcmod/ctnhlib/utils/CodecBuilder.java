package tech.vixhentx.mcmod.ctnhlib.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonnullType;

public class CodecBuilder<T extends S, P, S>
                         extends AbstractBuilder<Codec<? extends S>, Codec<? extends T>, P, CodecBuilder<T, P, S>> {

    private final Codec<? extends T> data;

    public CodecBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                        ResourceKey<Registry<Codec<? extends S>>> registryKey, Codec<? extends T> data) {
        super(owner, parent, name, callback, registryKey);
        this.data = data;
    }

    @Override
    protected @NonnullType Codec<? extends T> createEntry() {
        return data;
    }
}
