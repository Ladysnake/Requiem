package ladysnake.dissolution.common.config;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringChecker {
    private static final Pattern REGEX_STRING = Pattern.compile("/(.+)/");

    private String model;
    private Pattern regex;

    public static StringChecker from(String model) {
        Matcher matcher = REGEX_STRING.matcher(model);
        if (matcher.matches()) {
            return new StringChecker(matcher.group(1), true);
        }
        return new StringChecker(model, false);
    }

    private StringChecker(String model, boolean isRegex) {
        this.model = model;
        if (isRegex)
            regex = Pattern.compile(model);
    }

    public boolean matches(String tested) {
        return this.regex != null && this.regex.matcher(tested).matches() || Objects.equals(model, tested);
    }
}
