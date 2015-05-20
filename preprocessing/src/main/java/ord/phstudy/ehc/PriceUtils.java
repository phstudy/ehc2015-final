package ord.phstudy.ehc;

import com.google.common.collect.Maps;
import file.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by study on 5/20/15.
 */
public class PriceUtils {
    public static Map<String, Integer> prices = Maps.newConcurrentMap();

    static {
        try {
            ClassLoader classLoader = PriceUtils.class.getClassLoader();
            File file = new File(classLoader.getResource("product.csv").getFile());
            BufferedReader trainBr = FileManager.fileAsReader(file);
            String line;

            while((line = trainBr.readLine())!= null) {
                String[] part = line.split(",");
                String uid = part[0];
                int price = Integer.parseInt(part[1]);
                prices.put(uid, price);
            }
            System.out.println("prices init.");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
