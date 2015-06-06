package org.qty.validate;

import static org.qty.QLabInitConfig.NO_PID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;

public class ValidationUtils {

    public static <K, V> List<Entry<K, V>> mapToListAscOrder(Map<K, V> map) {
        return mapToList(map, true);
    }

    public static <K, V> List<Entry<K, V>> mapToListDescOrder(Map<K, V> map) {
        return mapToList(map, false);
    }

    public static <K, V> List<Entry<K, V>> mapToList(Map<K, V> map, final boolean ascOrder) {
        ArrayList<Entry<K, V>> list = new ArrayList<Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Entry<K, V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return ascOrder ? doCompare(o1, o2) : doCompare(o2, o1);
            }

            protected int doCompare(Entry<K, V> o1, Entry<K, V> o2) {
                Object v = o1.getValue();
                if (v instanceof Integer) {
                    Integer v1 = (Integer) o1.getValue();
                    Integer v2 = (Integer) o2.getValue();
                    return v1 - v2;
                }
                if (v instanceof Long) {
                    Long v1 = (Long) o1.getValue();
                    Long v2 = (Long) o2.getValue();
                    return (int) (v1 - v2);
                }
                if (v instanceof Float) {
                    Float v1 = (Float) o1.getValue();
                    Float v2 = (Float) o2.getValue();
                    return (int) (v1 - v2);
                }
                if (v instanceof Double) {
                    Double v1 = (Double) o1.getValue();
                    Double v2 = (Double) o2.getValue();
                    return (int) (v1 - v2);
                }
                throw new RuntimeException("unsupported type: " + v.getClass());
            }
        });
        return list;
    }

    public static void main(String[] args) {
        Map<String, Integer> foo = new HashMap<String, Integer>();
        foo.put("xd", 13);
        foo.put("orz", 413);
        foo.put("ordz", 3);

        System.out.println(ValidationUtils.mapToListAscOrder(foo));

    }

    public static String eruid(String line) {
        String s = StringUtils.substringBetween(line, "erUid=", ";");
        return Optional.fromNullable(s).or("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
    }

    public static String pid(String line) {
        String s = StringUtils.substringBetween(line, "pid=", ";");
        return Optional.fromNullable(s).or(NO_PID);
    }

}
