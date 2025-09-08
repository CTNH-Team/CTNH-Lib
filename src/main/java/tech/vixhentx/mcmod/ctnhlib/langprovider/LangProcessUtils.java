package tech.vixhentx.mcmod.ctnhlib.langprovider;

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
        Localized localized = field.getAnnotation(Localized.class);
        return TranslatedLang.of(localized.value()[0]);
    }
    static TranslatedLang[] getLocatedInfos(Field field){
        Localized localized = field.getAnnotation(Localized.class);
        return Stream.of(localized.value())
                .map(TranslatedLang::of)
                .toArray(TranslatedLang[]::new);
    }
}
