package org.qty.validate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.phstudy.ehc.utils.PriceUtils;

public class StatTrainingData {

    protected static String extraceTitle(String key) {
        int price = -1;

        try {
            if (PriceUtils.prices.containsKey(key)) {
                price = PriceUtils.prices.get(key);
            }
            URL u = new URL(
                    "http://shopping.udn.com/mall/cus/cat/Cc1c10.do?dc_btn_0=Func_FormalPreview&dc_cargxuid_0=U"
                            + key.substring(0, key.length() - 1));
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();

            boolean priceFlag = false;
            String title = null;
            for (String line : IOUtils.readLines(huc.getInputStream())) {
                if (priceFlag) {
                    String priceLine = StringUtils.substringBetween(line, ">", "<");
                    // 只留數字
                    price = NumberUtils.toInt(priceLine.replaceAll("[^0-9]+", ""), -1);
                    priceFlag = false;
                }
                if (StringUtils.contains(line, "title>")) {
                    title = StringUtils.substringBetween(line, "title>", "</titl");
                }
                if (StringUtils.contains(line, "網路價：")) {
                    priceFlag = true;
                }
            }

            return price + "," + title;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return price + ",unknown";
    }

    public static void main(String[] args) throws IOException {
        Iterator<String> it = IOUtils.lineIterator(
                StatTrainingData.class.getResourceAsStream("/traindata_hadoop_stat.txt"), "utf-8");

        Map<String, Long> m = new HashMap<String, Long>();
        while (it.hasNext()) {
            String s = it.next();
            String[] ss = s.split("\t");
            m.put(ss[0], Long.valueOf(ss[1]));
        }

        ArrayList<Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(m.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Long>>() {

            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return (int) (o2.getValue() - o1.getValue());
            }
        });

        for (Entry<String, Long> entry : list.subList(0, 20)) {
            System.out.println(String.format("%-16s\t%d\t%s", entry.getKey(), entry.getValue(),
                    extraceTitle(entry.getKey())));
        }
    }

}
