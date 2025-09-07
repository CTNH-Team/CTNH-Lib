package tech.vixhentx.mcmod.ctnhlib.langprovider;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import tech.vixhentx.mcmod.ctnhlib.langprovider.annotation.Domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DomainList  {
    final Object2ObjectOpenHashMap<String, List<Class<?>>> domainMap = new Object2ObjectOpenHashMap<>();
    public void put(String domain, Class<?>... objects)
    {
        domainMap.computeIfAbsent(domain, __->new ArrayList<>())
                .addAll(List.of(objects));
    }
    public List<Class<?>> get(String domain)
    {
        return domainMap.get(domain);
    }
    public ObjectSet<Map.Entry<String, List<Class<?>>>> entries()
    {
        return domainMap.entrySet();
    }
    public void dispose()
    {
        domainMap.clear();
    }

    ///Unrecommended usage
    public void put(Class<?>... classes)
    {
        for(Class<?> clazz : classes){
            var domainAnnotation = clazz.getAnnotation(Domain.class);
            if(domainAnnotation!= null){
                put(domainAnnotation.value(), clazz);
            }
        }
    }
}
