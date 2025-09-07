package tech.vixhentx.mcmod.ctnhlib.langprovider.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Domain {
    /// Must be lowercase
    String value();
    ///Processor Mod Id by default
    String root() default "";
    /// This class name, if not specified, it will be the class name
    String category() default "";
}
