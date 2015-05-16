package function;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Created by study on 5/15/15.
 */
public class Category {
    public static Function<String, String> toCategory = (line) -> {
        int start = line.indexOf("cat=") + "cat=".length();
        int end = line.indexOf(";", start);

        return line.substring(start, end);
    };

    public static ToIntFunction<String> toCount = categories -> categories.split(",").length;

    public static Predicate<String> containCategory = line -> line.contains("cat=");

}

