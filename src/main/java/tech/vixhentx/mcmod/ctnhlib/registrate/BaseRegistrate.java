package tech.vixhentx.mcmod.ctnhlib.registrate;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.*;
import tech.vixhentx.mcmod.ctnhlib.langprovider.LangProcessor;

import java.util.function.Function;

public class BaseRegistrate<S extends BaseRegistrate<S,G>, G extends GTRegistrate> extends AbstractRegistrate<S> {
    /**
     * Construct a new Registrate for the given mod ID.
     *
     * @param modid The mod ID for which objects will be registered
     */
    protected BaseRegistrate(String modid, Function<String, G> GTRegistrateFactory) {
        super(modid);
        this.langProcessor = new LangProcessor(this);
        this.gtRegistrate = GTRegistrateFactory.apply(modid);
        gtRegistrate.registerRegistrate();
    }
    //wrap a langprocessor
    private final LangProcessor langProcessor;
    //warp a gtregistrate
    private final G gtRegistrate;
    public G GT(){
        return gtRegistrate;
    }

    public S addLang(Class<?> clazz){
        langProcessor.process(clazz);
        return self();
    }
}
