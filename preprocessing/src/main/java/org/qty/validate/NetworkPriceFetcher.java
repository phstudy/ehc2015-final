package org.qty.validate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.phstudy.ehc.utils.PriceUtils;

@SuppressWarnings("unchecked")
public class NetworkPriceFetcher {

    public final static String priceCached = "price.cache.data";

    private static Map<String, Integer> cachedPrices = new HashMap<String, Integer>();

    static {
        try {
            File data = cachePath();
            if (!data.exists()) {
                cachedPrices = new HashMap<String, Integer>(PriceUtils.prices);
                writeToFile(data);
            } else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cachePath()));
                cachedPrices = ((Map<String, Integer>) ois.readObject());
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static File cachePath() {
        File cacheInWorkingDir = new File(priceCached);
        if(cacheInWorkingDir.exists()){
            System.out.println("using price-cache: " + cacheInWorkingDir);
            return cacheInWorkingDir;
        }
        return new File(System.getProperty("user.home"), priceCached);
    }

    public static Map<String, Integer> buildPriceSet(Set<String> pids) {
        Map<String, Integer> subset = new HashMap<String, Integer>();
        for (String pid : pids) {
            try {
                Integer price = lookPrice(pid);
                if (price == null || price <= 0) {
                    continue;
                }
                subset.put(pid, price);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return subset;
    }

    public static Integer lookPrice(String pid) throws FileNotFoundException, IOException {

        if (cachedPrices.containsKey(pid)) {
            return cachedPrices.get(pid);
        }

        Integer networkPrice = fetchPrice(pid);
        if (networkPrice != null) {
            cachedPrices.put(pid, networkPrice);
        } else {
            // 如果沒有修改抓資料的方法，抓不到的永遠抓不到，所以就存 -1 唄
            // 先不管是抓不到，或是沒有資料了
            cachedPrices.put(pid, -1);
        }

        //        if (networkPrice == -1) {
        //            return null;
        //        }

        return networkPrice;
    }

    public static void savePriceState() {
        try {
            writeToFile(cachePath());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Integer fetchPrice(String key) {
        Integer price = null;

        try {

            URL u = new URL(
                    "http://shopping.udn.com/mall/cus/cat/Cc1c10.do?dc_btn_0=Func_FormalPreview&dc_cargxuid_0=U"
                            + key.substring(0, key.length() - 1));
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();

            boolean priceFlag = false;
            for (String line : IOUtils.readLines(huc.getInputStream())) {
                if (priceFlag) {
                    String priceLine = StringUtils.substringBetween(line, ">", "<");
                    // 只留數字
                    price = NumberUtils.toInt(priceLine.replaceAll("[^0-9]+", ""), -1);
                    priceFlag = false;
                }
                if (StringUtils.contains(line, "網路價：")) {
                    priceFlag = true;
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("get " + key + " => " + price);
        return price;
    }

    public static void update(String pid, String price) {
        try {
            cachedPrices.put(pid, NumberUtils.toInt(price));
        } catch (Exception e) {
        }
    }

    protected static void writeToFile(File data) throws IOException, FileNotFoundException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(data));
        oos.writeObject(cachedPrices);
        oos.close();
    }

}
