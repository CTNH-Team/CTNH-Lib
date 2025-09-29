package tech.vixhentx.mcmod.ctnhlib.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTBlockBuilder;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.apache.commons.lang3.tuple.Pair;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;
import tech.vixhentx.mcmod.ctnhlib.registrate.builders.CTNHBlockBuilder;
import tech.vixhentx.mcmod.ctnhlib.registrate.builders.CTNHItemBuilder;

import java.util.ArrayList;
import java.util.List;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;
import static tech.vixhentx.mcmod.ctnhlib.utils.EnvUtils.isDataGen;

public class CNRegistrate extends GTRegistrate {
    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modid The mod ID for which objects will be registered
     */
    protected CNRegistrate(String modid) {
        super(modid);
        this.langProcessor = new LangProcessor(this);
    }

    public <T extends Item> CTNHItemBuilder<T, GTRegistrate> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return item(self(), name, factory);
    }


    public <T extends Item, P> CTNHItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        return (CTNHItemBuilder<T, P>) entry(name, callback -> CTNHItemBuilder.create(this, parent, name, callback, factory));
    }

    @Override
    public <T extends Block> CTNHBlockBuilder<T, GTRegistrate> block(String name,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    @Override
    public <T extends Block, P> CTNHBlockBuilder<T, P> block(P parent, String name,
                                                           NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (CTNHBlockBuilder<T, P>) entry(name,
                callback -> CTNHBlockBuilder.create(this, parent, name, callback, factory));
    }

    private final LangProcessor langProcessor;
    private final ObjectSet<Class<?>> langProcessed = new ObjectOpenHashSet<>();

    //Chinese
    private final NonNullSupplier<List<Pair<String, String>>> extraCNLang = NonNullSupplier.lazy(() -> {
        final List<Pair<String, String>> ret = new ArrayList<>();
        addDataGenerator(CNLANG, prov -> ret.forEach(p -> prov.add(p.getKey(), p.getValue())));
        return ret;
    });

    public MutableComponent addCNLang(String type, ResourceLocation id, String localizedName) {
        return addRawCNLang(Util.makeDescriptionId(type, id), localizedName);
    }

    public MutableComponent addCNLang(String type, ResourceLocation id, String suffix, String localizedName) {
        return addRawCNLang(Util.makeDescriptionId(type, id) + "." + suffix, localizedName);
    }

    public MutableComponent addRawCNLang(String key, String value) {
        if(isDataGen) {
            extraCNLang.get().add(Pair.of(key, value));
        }
        return Component.translatable(key);
    }

    //English and Chinese
    public MutableComponent addLang(String type, ResourceLocation id, String en, String cn) {
        addRawLang(Util.makeDescriptionId(type, id), en);
        return addRawCNLang(Util.makeDescriptionId(type, id), cn);
    }

    public MutableComponent addLang(String type, ResourceLocation id, String suffix, String en, String cn) {
        addRawLang(Util.makeDescriptionId(type, id) + "." + suffix, en);
        return addRawCNLang(Util.makeDescriptionId(type, id) + "." + suffix, cn);
    }

    public void addRawLang(String key, String en, String cn) {
        if(!en.isEmpty()) addRawLang(key, en);
        if(!cn.isEmpty()) addRawCNLang(key, cn);
    }

    public CNRegistrate addLangProcessor() {
        this.addDataGenerator(ProviderType.LANG, prov -> {
            LangProcessor processor = new LangProcessor(this);
            processor.processAll();
        });
        return this;
    }

    //Lang Processor
    /// @param clazz the class to process lang
//    public CNRegistrate addLang(Class<?> clazz){
//        if(langProcessed.add(clazz))
//            langProcessor.process(clazz);
//        return this;
//    }
}
