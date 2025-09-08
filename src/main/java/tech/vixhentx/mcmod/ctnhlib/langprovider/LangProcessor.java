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
        String category="", domain="", className, root="";
        LinkedList<String> prefixes = new LinkedList<>();
        LinkedList<String> suffixes = new LinkedList<>();

        className = clazz.getSimpleName();

        Domain domainAnnotation = clazz.getAnnotation(Domain.class);
        if(domainAnnotation != null) {
            category = LangProcessUtils.getCategory(domainAnnotation, className);
            domain = domainAnnotation.value();
            root = LangProcessUtils.getRoot(domainAnnotation, modid);
        }
        Prefix prefixAnnotation = clazz.getAnnotation(Prefix.class);
        if(prefixAnnotation != null) {
            String prefix = LangProcessUtils.getPrefix(prefixAnnotation,clazz::getSimpleName);
            prefixes.addLast(prefix);
        }
        Suffix suffixAnnotation = clazz.getAnnotation(Suffix.class);
        if(suffixAnnotation != null) {
            String suffix = LangProcessUtils.getSuffix(suffixAnnotation,clazz::getSimpleName);
            suffixes.addLast(suffix);
        }

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
            Suffix suffix = clazzInClazz.getAnnotation(Suffix.class);
            if(prefix != null || suffix != null){
                if(prefix!= null) prefixes.addLast(LangProcessUtils.getPrefix(prefix,clazzInClazz::getSimpleName));
                if(suffix != null) suffixes.addLast(LangProcessUtils.getSuffix(suffix,clazzInClazz::getSimpleName));
                processCurrent(clazzInClazz, prefixes, suffixes, domain, root, category);
                if(prefix != null) prefixes.removeLast();
                if(suffix != null) suffixes.removeLast();
            }
        }
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
