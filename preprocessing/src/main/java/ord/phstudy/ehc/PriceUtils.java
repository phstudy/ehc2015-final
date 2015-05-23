package ord.phstudy.ehc;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Maps;

/**
 * Created by study on 5/20/15.
 */
public class PriceUtils {
    public static Map<String, Integer> prices = Maps.newConcurrentMap();

    static {
        loadPriceFromData("/product.csv");
        loadPriceFromData("/product2.csv");
        loadPriceFromData("/product3.csv");
    }

    public static void loadPriceFromData(String classpathLocation) {
        InputStream in = PriceUtils.class.getResourceAsStream(classpathLocation);
        try {
            Iterator<String> it = IOUtils.lineIterator(in, Charset.defaultCharset());
            while (it.hasNext()) {
                String[] part = it.next().split(",");
                String uid = part[0];
                int price = Integer.parseInt(part[1]);
                prices.put(uid, price);
            }
            System.out.println("load product prices: " + classpathLocation);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
        }

    }
}
