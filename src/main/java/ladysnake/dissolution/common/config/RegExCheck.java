package ladysnake.dissolution.common.config;

import org.intellij.lang.annotations.Language;

import javax.annotation.RegEx;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegExCheck {
    @RegEx
    @Language("RegExp") String value();
}
