package org.qty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.phstudy.ehc.utils.PriceUtils;

import com.google.common.base.Joiner;

public class PrepareProbingData {

    public static void main(String[] args) throws Exception {
        generateProberList("prober", 10);
        generateProberList("prober13", 13);
    }

    protected static void generateProberList(String dir, int pidLength) throws IOException {
        InputStream input = PrepareProbingData.class.getResourceAsStream("/traindata_hadoop_stat.txt");
        Iterator<String> it = IOUtils.lineIterator(input, "utf-8");
        Map<String, Long> data = new HashMap<String, Long>();
        while (it.hasNext()) {
            String s = it.next();
            String[] kv = s.split("[ \t]+");
            data.put(kv[0], Long.valueOf(kv[1]));
        }

        List<Entry<String, Long>> list = new ArrayList<Entry<String, Long>>(data.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Long>>() {

            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return (int) (o2.getValue() - o1.getValue());
            }
        });

        Iterator<Entry<String, Long>> filterIt = list.iterator();
        while (filterIt.hasNext()) {
            if (filterIt.next().getKey().length() != pidLength) {
                filterIt.remove();
            }
        }

        new File(dir).mkdirs();
        for (int i = 0; i < 100; i += 20) {
            System.out.println(i);
            int startIndex = i;
            int endIndex = startIndex + 20;

            ArrayList<String> udnIDfileContent = new ArrayList<String>();
            ArrayList<String> fileContent = new ArrayList<String>();
            int rank = 1;
            for (Entry<String, Long> entry : list.subList(startIndex, endIndex)) {
                //                fileContent.add(String.format("%02d,%s", rank, entry.getKey()));
                String key = entry.getKey();
                fileContent.add(String.format("%s", entry.getKey()));
                udnIDfileContent.add(extraceTitle(key));
                rank++;
            }

            Writer writer = new FileWriter(String.format(dir + "/%03d_%03d.txt", startIndex, endIndex));
            writer.write(Joiner.on("\n").join(fileContent));
            writer.close();

            Writer udnWriter = new FileWriter(String.format(dir + "/UDN_%03d_%03d.txt", startIndex, endIndex));
            udnWriter.write(Joiner.on("\n").join(udnIDfileContent));
            udnWriter.close();
        }
    }

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

            return key + "," + price + "," + title;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return key + "," + price + ",unknown";
    }

}
