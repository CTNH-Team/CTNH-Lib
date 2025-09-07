package tech.vixhentx.mcmod.ctnhlib.langprovider;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.*;

import java.lang.reflect.Field;
import java.util.*;

class LangProcessUtils {
    static String getRoot(Class<?> clazz,String orElse){
        var root = clazz.getAnnotation(Domain.class).root();
        if(root.isEmpty()) return orElse;
        else return root;
    }
    static String getCategory(Class<?> clazz, String domain){
        var category = clazz.getAnnotation(Domain.class).category();
        if(!category.isEmpty()) return category;

        String rawName = clazz.getSimpleName().toLowerCase(Locale.ROOT);
        if(rawName.endsWith(domain)) return rawName.substring(0, rawName.length() - domain.length());
        else if (rawName.startsWith(domain)) return rawName.substring(domain.length() + 1);
        else return rawName;
    }
    static String getPrefix(Class<?> clazz){
        var prefix = clazz.getAnnotation(Prefix.class).value();
        return prefix.isEmpty()? clazz.getSimpleName().toLowerCase(Locale.ROOT) : prefix;
    }
    static String getSuffix(Class<?> clazz){
        var suffix = clazz.getAnnotation(Suffix.class).value();
        return suffix.isEmpty()? clazz.getSimpleName().toLowerCase(Locale.ROOT) : suffix;
    }
    static String getItemKey(Field field){
        var key = field.getAnnotation(Key.class);
        if(key == null) return field.getName().toLowerCase(Locale.ROOT);
        else return key.value();
    }
    static String buildKey(List<String> prefixes, List<String> suffixes, String domain, String root, String category, String item) {
        StringJoiner builder = new StringJoiner(".");

        for (var prefix : prefixes)
            builder.add(prefix);

        builder.add(domain)
                .add(root)
                .add(category);

        for (var suffix : suffixes)
            builder.add(suffix);

        builder.add(item);
        return builder.toString();
    }
    static String buildKeyWithIndex(String builtKey, int index){
        return builtKey + "." + index;
    }
    @SuppressWarnings("AccessStaticViaInstance")
    static LocatedLang getLocatedInfo(Field field){
        var builder = LocatedLang.builder();
        var generals = field.getAnnotationsByType(Locate.class);
        for (var general : generals) {
            var values = general.value();
            var location = general.location();
            for (var value : values)
                builder.add(location, value);
        }

        //specified
        var en = field.getAnnotation(Locate.EN.class);
        if(en!=null)
                builder.add(en.LOCATION, en.value()[0]);

        var cn = field.getAnnotation(Locate.CN.class);
        if(cn!=null)
                builder.add(cn.LOCATION, cn.value()[0]);

        return builder.build();
    }
    @SuppressWarnings("AccessStaticViaInstance")
    static LocatedLang[] getLocatedInfos(Field field){
        var generals = field.getAnnotationsByType(Locate.class);
        Map<String, String[]> langMap = new Object2ObjectArrayMap<>();
        for (var general : generals) {
            var values = general.value();
            var location = general.location();
            langMap.put(location, values);
        }

        //specified
        var en = field.getAnnotation(Locate.EN.class);
        if(en!=null)
            langMap.put(en.LOCATION, en.value());

        var cn = field.getAnnotation(Locate.CN.class);
        if(cn!=null)
            langMap.put(cn.LOCATION, cn.value());

        int size = langMap.values().stream().mapToInt(a->a.length).max().orElse(0);

        LocatedLang[] ret = new LocatedLang[size];
        for(int i=0;i<size;i++) ret[i].locates = new Located[langMap.size()];

        int index = 0;
        for(var entry : langMap.entrySet()){
            var location = entry.getKey();
            var values = entry.getValue();
            for(int i=0;i<values.length;i++)
                ret[i].locates[index++] = new Located(location, values[i]);
        }

        return ret;
    }
}
