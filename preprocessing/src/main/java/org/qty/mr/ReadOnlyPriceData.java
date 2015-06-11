package org.qty.mr;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

@SuppressWarnings("unchecked")
public class ReadOnlyPriceData {

    static Map<String, Integer> cachedPrices = new HashMap<String, Integer>();
    static {

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(ReadOnlyPriceData.class.getResourceAsStream("/price.cache.data"));
            cachedPrices = ((Map<String, Integer>) ois.readObject());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(ois);
        }

    }

    public static Integer lookUp(String pid) {
        if (cachedPrices.containsKey(pid)) {
            int v = cachedPrices.get(pid);
            if (v < 0) {
                return 0;
            }
            return v;
        }
        return 0;
    }

}
