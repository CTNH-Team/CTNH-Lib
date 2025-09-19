package tech.vixhentx.mcmod.ctnhlib.langprovider;

import tech.vixhentx.mcmod.ctnhlib.CTNHLib;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

class LangProcessUtils {
    static String getRoot(Domain domain, String defaultRoot){
        var root = domain.root();
        if(root.isEmpty()) return defaultRoot;
        else return root;
    }
    static String getCategory(Domain domain, String className){
        var category = domain.category();
        if(!category.isEmpty()) return category;

        String rawName = className.toLowerCase(Locale.ROOT);
        String domainName = domain.value();
        if(rawName.endsWith(domainName)) return rawName.substring(0, rawName.length() - domainName.length());
        else if (rawName.startsWith(domainName)) return rawName.substring(domainName.length() + 1);
        else return rawName;
    }
    static String getPrefix(Prefix prefix, Supplier<String> className){
        return prefix.value().isEmpty()? className.get().toLowerCase(Locale.ROOT) : prefix.value();
    }
    static String getSuffix(Suffix suffix, Supplier<String> className){
        return suffix.value().isEmpty()? className.get().toLowerCase(Locale.ROOT) : suffix.value();
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

        if(!domain.isEmpty())
            builder.add(domain);
        if(!root.isEmpty())
            builder.add(root);
        if(!category.isEmpty())
            builder.add(category);

        for (var suffix : suffixes)
            builder.add(suffix);

        builder.add(item);
        return builder.toString();
    }
    static String buildKeyWithIndex(String builtKey, int index){
        return builtKey + "." + index;
    }
    static TranslatedLang getLocatedInfo(Field field){
        String[] en = field.getAnnotation(EN.class).value();
        String[] cn = field.getAnnotation(CN.class).value();
        if(en==null&&cn==null) throw new IllegalArgumentException("Lang must have @EN or @CN annotation, or TranslatedLang literal.");
        return new TranslatedLang(ofNullable(en,()->warnForFieldNoValue(field)), ofNullable(cn,()->warnForFieldNoValue(field)));
    }
    static TranslatedLang[] getLocatedInfos(Field field){
        String[] ens = field.getAnnotation(EN.class).value();
        String[] cns = field.getAnnotation(CN.class).value();
        if(ens.length!= cns.length) warnForFieldIncorrectCount(field);
        var ret = new TranslatedLang[ens.length];
        for(int i = 0; i < Integer.min(ens.length, cns.length); i++){
            ret[i] = new TranslatedLang(ofNullable(ens[i],()->warnForFieldNoValue(field)), ofNullable(cns[i],()->warnForFieldNoValue(field)));
        }
        return ret;
    }
    static String ofNullable(String[] str, Runnable nullWarning){
        if(str==null){
            nullWarning.run();
            return "";
        }
        return str[0];
    }
    static String ofNullable(String str, Runnable nullWarning){
        if(str==null){
            nullWarning.run();
            return "";
        }
        return str;
    }
    static void warnForFieldNoValue(Field field){
        CTNHLib.LOGGER.error("Field {} in class {} has no @EN or @CN annotation, or TranslatedLang literal.", field.getName(), field.getDeclaringClass().getName());
    }
    static void warnForFieldIncorrectCount(Field field){
        CTNHLib.LOGGER.error("The number of @EN and @CN annotations must be the same in field {} in class {}", field.getName(), field.getDeclaringClass().getName());
    }
    static void warnForIncorrectCount(){
        CTNHLib.LOGGER.error("The number of en lang and cn lang must be the same in all fields.");
    }
}
