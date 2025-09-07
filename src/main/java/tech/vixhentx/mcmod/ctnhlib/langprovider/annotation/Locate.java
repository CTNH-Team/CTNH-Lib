package tech.vixhentx.mcmod.ctnhlib.langprovider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Locate {
    String location();
    String[] value();

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface EN{
        String[] value();
        String LOCATION = "en_us";
    }
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface CN{
        String[] value();
        String LOCATION = "zh_cn";
    }
    // add more if needed
}
