package tech.vixhentx.mcmod.ctnhlib.langprovider;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraftforge.fml.loading.FMLLoader;
import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public class LangProcessor {
    final String modid;
    public final DomainList domainList = new DomainList();
    public final Map<String, Map<String, String>> processedData = new HashMap<>();
    private final Consumer<LocatedLang> genDataMethod;

    public LangProcessor(String modid){
        this.modid = modid;
        boolean isDataGen = FMLLoader.getLaunchHandler().isData();
        genDataMethod = isDataGen? this::genLangData : __ -> {};
    }
    public void dispose(){
        domainList.dispose();
    }
    public void genForLocation(String location, RegistrateLangProvider provider){
        for(var entry : processedData.get(location).entrySet()){
            provider.add(entry.getKey(), entry.getValue());
        }
        processedData.remove(location);
    }
    public void process(){
        for(var entry : domainList.entries()){
            var domain = entry.getKey();
            var classes = entry.getValue();
            for(var clazz : classes){
                String category = LangProcessUtils.getCategory(clazz,domain);
                var root = LangProcessUtils.getRoot(clazz, modid);

                LinkedList<String> prefixes = new LinkedList<>();
                LinkedList<String> suffixes = new LinkedList<>();

                //dfs
                processCurrent(clazz, prefixes, suffixes, domain, root, category);
            }
        }
        domainList.dispose();
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

        for (var clazzInClazz : current.getDeclaredClasses())
            if (clazzInClazz.isAnnotationPresent(Prefix.class))
                processPrefix(clazzInClazz, prefixes, suffixes, domain, root, category);
            else if (clazzInClazz.isAnnotationPresent(Suffix.class))
                processSuffix(clazzInClazz, prefixes, suffixes, domain, root, category);
    }
    private void processPrefix(Class<?> clazz, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category){
        String prefix = LangProcessUtils.getPrefix(clazz);
        prefixes.addLast(prefix);
        processCurrent(clazz, prefixes, suffixes,domain, root, category);
        prefixes.removeLast();
    }
    private void processSuffix(Class<?> clazz, LinkedList<String> prefixes, LinkedList<String> suffixes, String domain, String root, String category){
        String suffix = LangProcessUtils.getSuffix(clazz);
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
            LocatedLang locatedLang;
            if(value instanceof LocatedLang locatedValue){
                locatedLang = locatedValue;
            } else { // it must be null, read locate annotation
                locatedLang = LangProcessUtils.getLocatedInfo(field);
            }

            locatedLang.key = builtKey;
            genDataMethod.accept(locatedLang);

            //erase the detailed location info
            field.set(holderClass, locatedLang.erase());
            field.setAccessible(false);
        }else{
            field.setAccessible(true);
            Lang[] array = (Lang[]) field.get(holderClass);
            LocatedLang[] locatedLangs;
            if(array instanceof LocatedLang[] locatedArray){
                locatedLangs = locatedArray;
            } else { // it must be null, read locate annotation
                locatedLangs = LangProcessUtils.getLocatedInfos(field);
            }

            for(int i = 0; i < locatedLangs.length; i++) {
                locatedLangs[i].key = LangProcessUtils.buildKeyWithIndex(builtKey, i);
                genDataMethod.accept(locatedLangs[i]);
            }

            //erase the detailed location info
            Lang[] erasedArray = new Lang[locatedLangs.length];
            for(int i = 0; i < locatedLangs.length; i++) erasedArray[i] = locatedLangs[i].erase();
            field.set(holderClass, erasedArray);
            field.setAccessible(false);
        }
    }
    private void genLangData(LocatedLang lang){
        for(var locate : lang.locates)
            processedData.computeIfAbsent(locate.location(), __ -> new Object2ObjectArrayMap<>()).put(lang.key, locate.value());
    }
}
