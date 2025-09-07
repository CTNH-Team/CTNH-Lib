package tech.vixhentx.mcmod.ctnhlib.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.*;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;

import java.util.function.Function;

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

    public BaseRegistrate addLang(Class<?> clazz){
        langProcessor.process(clazz);
        return this;
    }
}
