package function;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Created by study on 5/15/15.
 */
public class Plist {
    public static Function<String, String> toPlist = (line) -> {
        int start = line.indexOf("plist=") + "plist=".length();
        int end = line.indexOf(";", start);

        return line.substring(start, end);
    };

    public static ToIntFunction<String> toUniqueCount = plist -> plist.split(",").length / 3;

    public static ToIntFunction<String> toCount = plist -> {
        String[] plists = plist.split(",");

        int count = 0;
        if(plists.length > 1) {
            for (int i = 0; i < plists.length; i += 3) {
                count += Integer.parseInt(plists[i + 1]);
            }
        }

        return count;
    };

    public static Predicate<String> containPlist = line -> line.contains("plist=");
}
