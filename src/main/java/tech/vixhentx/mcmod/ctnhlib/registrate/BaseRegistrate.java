package tech.vixhentx.mcmod.ctnhlib.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;

import java.util.ArrayList;
import java.util.List;

import static tech.vixhentx.mcmod.ctnhlib.registrate.data.ProviderTypes.CNLANG;
import static tech.vixhentx.mcmod.ctnhlib.utils.EnvUtils.isDataGen;

public class BaseRegistrate extends GTRegistrate {
    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modid The mod ID for which objects will be registered
     */
    protected BaseRegistrate(String modid) {
        super(modid);
        this.langProcessor = new LangProcessor(this);
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

    public MutableComponent addRawLang(String key, String en, String cn) {
        addRawLang(key, en);
        return addRawCNLang(key, cn);
    }

    //Lang Processor
    /// @param clazz the class to process lang
    public BaseRegistrate addLang(Class<?> clazz){
        if(langProcessed.add(clazz))
            langProcessor.process(clazz);
        return this;
    }
}
