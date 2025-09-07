package tech.vixhentx.mcmod.ctnhlib.langprovider;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraftforge.fml.loading.FMLLoader;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class LangProcessor {
    final String modid;
    private final Consumer<TranslatedLang> genDataMethod;

    public LangProcessor(String modid, Consumer<TranslatedLang> dataGenerator){
        this.modid = modid;
        boolean isDataGen = FMLLoader.getLaunchHandler().isData();
        genDataMethod = isDataGen? dataGenerator : __ -> {};
    }
    public LangProcessor(AbstractRegistrate<?> registrate){
        this(registrate.getModid(),(lang)->registrate.addRawLang(lang.key, lang.translation));
    }

    public void process(Class<?> clazz){
        String category, domain, className;
        className = clazz.getSimpleName();
        Domain domainAnnotation = clazz.getAnnotation(Domain.class);
        category = LangProcessUtils.getCategory(domainAnnotation,className);
        domain = domainAnnotation.value();
        var root = LangProcessUtils.getRoot(domainAnnotation, modid);

        LinkedList<String> prefixes = new LinkedList<>();
        LinkedList<String> suffixes = new LinkedList<>();

        //dfs
        processCurrent(clazz, prefixes, suffixes, domain, root, category);
    }
    private void processCurrent(Class<?> current, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category){
        for (var field : current.getDeclaredFields())
            try {
                if (field.getType() == Lang.class)
                    processLang(field, current, false, prefixes, suffixes, domain, root, category);
                else if (field.getType().getComponentType() == Lang.class)
                    processLang(field, current, true, prefixes, suffixes, domain, root, category);
            } catch (IllegalAccessException e) {
                CTNHLib.LOGGER.error("Illegal access to field {} in class {}", field.getName(), current.getName());
            }

        for (var clazzInClazz : current.getDeclaredClasses()){
            Prefix prefix = clazzInClazz.getAnnotation(Prefix.class);
            if(prefix!= null) processPrefix(clazzInClazz, prefix, prefixes, suffixes, domain, root, category);
            else {
                Suffix suffix = clazzInClazz.getAnnotation(Suffix.class);
                if (suffix != null) processSuffix(clazzInClazz, suffix, prefixes, suffixes, domain, root, category);
            }
        }
    }
    private void processPrefix(Class<?> clazz, Prefix prefixAnnotation, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category){
        String prefix = LangProcessUtils.getPrefix(prefixAnnotation,clazz::getSimpleName);
        prefixes.addLast(prefix);
        processCurrent(clazz, prefixes, suffixes,domain, root, category);
        prefixes.removeLast();
    }
    private void processSuffix(Class<?> clazz, Suffix suffixAnnotation, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category){
        String suffix = LangProcessUtils.getSuffix(suffixAnnotation,clazz::getSimpleName);
        suffixes.addLast(suffix);
        processCurrent(clazz, prefixes, suffixes, domain, root, category);
        suffixes.removeLast();
    }
    private void processLang(Field field,Class<?> holderClass, boolean isArray, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category)
            throws IllegalAccessException {
        String itemKey = LangProcessUtils.getItemKey(field);
        String builtKey = LangProcessUtils.buildKey(prefixes, suffixes,domain,root,category,itemKey);

        if(!isArray){
            field.setAccessible(true);
            Lang value = (Lang) field.get(holderClass);
            TranslatedLang translatedLang;
            if(value instanceof TranslatedLang locatedValue){
                translatedLang = locatedValue;
            } else { // it must be null, read locate annotation
                translatedLang = LangProcessUtils.getLocatedInfo(field);
            }

            translatedLang.key = builtKey;
            genDataMethod.accept(translatedLang);

            //erase the detailed location info
            field.set(holderClass, translatedLang.erase());
            field.setAccessible(false);
        }else{
            field.setAccessible(true);
            Lang[] array = (Lang[]) field.get(holderClass);
            TranslatedLang[] translatedLangs;
            if(array instanceof TranslatedLang[] locatedArray){
                translatedLangs = locatedArray;
            } else { // it must be null, read locate annotation
                translatedLangs = LangProcessUtils.getLocatedInfos(field);
            }

            for(int i = 0; i < translatedLangs.length; i++) {
                translatedLangs[i].key = LangProcessUtils.buildKeyWithIndex(builtKey, i);
                genDataMethod.accept(translatedLangs[i]);
            }

            //erase the detailed location info
            Lang[] erasedArray = new Lang[translatedLangs.length];
            for(int i = 0; i < translatedLangs.length; i++) erasedArray[i] = translatedLangs[i].erase();
            field.set(holderClass, erasedArray);
            field.setAccessible(false);
        }
    }
}
