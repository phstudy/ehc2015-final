import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.ProductRecord;

public class Utility {

    public static Map<String, Object> requestToMap(String str) {
        Map<String, Object> rst = new HashMap<String, Object>(10);
        int lastChar = str.charAt(str.length() - 1);
        if (lastChar == ';') {
            str = str.substring(0, str.length() - 1);
        }
        String[] parts = str.split(";");

        for (String part : parts) {
            String[] elements = part.split("=");
            String key = elements[0];
            String value = "";
            if (elements.length == 2) {
                value = elements[1];
            }
            Object object = value;

            if ("keywords".equals(key)) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                    value = value.replace(',', '|');
                    object = value;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ("cat".equals(key)) {
                object = categoryToList(value);
            }

            if ("plist".equals(key)) {
                object = plistToList(value);
            }

            rst.put(key, object);
        }

        return rst;
    }

    public static List<String> categoryToList(String categories) {
        String[] parts = categories.split(",");
        List<String> rst;
        if (parts.length > 0 && parts[0].length() != 1) {
            rst = new ArrayList<String>(5);
            String[] cparts = parts[0].split("_");
            String str = cparts[0];
            rst.add(str);
            for (int i = 1; i < cparts.length; i++) {
                str += "_" + cparts[i];
                rst.add(str);
            }
        } else {
            rst = Arrays.asList(parts);
        }

        return rst;
    }

    public static List<ProductRecord> plistToList(String plist) {
        List<ProductRecord> rst = new ArrayList<ProductRecord>();

        String[] records = plist.split(",");

        if (records.length > 1) {
            for (int i = 0; i < records.length; i += 3) {
                try {
                    String pid = records[i];
                    int num = Integer.parseInt(records[i + 1]);
                    int price = Integer.parseInt(records[i + 2]);

                    rst.add(new ProductRecord(pid, num, price));
                } catch (Exception e) {
                    System.out.println(plist);
                    e.printStackTrace();
                }
            }
        }

        return rst;
    }

}
