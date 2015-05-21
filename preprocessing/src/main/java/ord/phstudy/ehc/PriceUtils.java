package ord.phstudy.ehc;

import com.google.common.collect.Maps;
import file.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.util.Map;

/**
 * Created by study on 5/20/15.
 */
public class PriceUtils {
    public static Map<String, Integer> prices = Maps.newConcurrentMap();

    static {
        try {
            ClassLoader classLoader = PriceUtils.class.getClassLoader();

            // http://iguang.tw/udn/product/<pid>.html
            File file = new File(classLoader.getResource("product.csv").getFile());
            BufferedReader trainBr = FileManager.fileAsReader(file);
            String line;

            while ((line = trainBr.readLine()) != null) {
                String[] part = line.split(",");
                String uid = part[0];
                int price = Integer.parseInt(part[1]);
                prices.put(uid, price);
            }
            System.out.println("product prices#1 init.");

            // http://www.cheapcheap.com.tw/kw?q=<desc>
            file = new File(classLoader.getResource("product2.csv").getFile());
            trainBr = FileManager.fileAsReader(file);

            while ((line = trainBr.readLine()) != null) {
                String[] part = line.split(",");
                String uid = part[0];
                int price = Integer.parseInt(part[1]);
                prices.put(uid, price);
            }
            System.out.println("product prices#2 init.");

            // http://pk.emailcash.com.tw/catalog.asp
            file = new File(classLoader.getResource("product3.csv").getFile());
            trainBr = FileManager.fileAsReader(file);

            while ((line = trainBr.readLine()) != null) {
                String[] part = line.split(",");
                String uid = part[0];
                int price = Integer.parseInt(part[1]);
                prices.put(uid, price);
            }
            System.out.println("product prices#3 init.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
