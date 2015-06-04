package org.qty;

import static org.qty.QLabInitConfig.NO_PID;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.qty.file.FileManager;
import org.qty.validate.NetworkPriceFetcher;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;

public class ProductData {

    static String eruid(String line) {
        String s = StringUtils.substringBetween(line, "erUid=", ";");
        return Optional.fromNullable(s).or("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
    }

    static String pid(String line) {
        String s = StringUtils.substringBetween(line, "pid=", ";");
        return Optional.fromNullable(s).or(NO_PID);
    }

    static String mainCategory(String line) {
        try {
            String s = StringUtils.substringBetween(line, "cat=", ";");
            return Optional.fromNullable("" + s.charAt(0)).or("_");
        } catch (Exception e) {
            return "_";
        }
    }

    public static void main(String[] args) throws Exception {

        String inputFile = args[0];
        String outputFile = args[1];
        Products products = new Products();
        //
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s : FileManager.fileAsLineIterator(inputFile)) {

            String eruid = eruid(s);
            String pid = pid(s);

            if (eruid.contains(" ")) {
                continue;
            }

            if (s.contains("act=o")) {
                products.order(s);
                continue;
            }

            if (!s.contains("act=v")) {
                continue;
            }

            String cat = mainCategory(s);
            if ("_".equals(cat)) {
                continue;
            }
            //            System.out.println(pid+"_" + cat);
            products.viewAction(pid, cat, eruid);

        }

        products.dump(outputFile);
        //        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
        //        String outfile = "qty.lab.csv";
        //        manager.dump(outfile);
        //        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
    }

    static class Products {

        ItemCounter<String> pidViewCount = new ItemCounter<String>();
        ItemCounter<String> orderCount = new ItemCounter<String>();
        ItemGroupCounter<String, String> pidUniqueViewCount = new ItemGroupCounter<String, String>();

        Map<String, String> catMap = new HashMap<String, String>();

        public void viewAction(String pid, String cat, String eruid) {
            pidViewCount.count(pid);
            pidUniqueViewCount.count(pid, eruid);

            if (!catMap.containsKey(pid)) {
                catMap.put(pid, cat);
            }
        }

        public void dump(String filename) throws Exception {

            Writer w = FileManager.fileAsWriter(filename);
            ArrayList<String> data = new ArrayList<String>();
            data.add("pid");
            data.add("view");
            data.add("viewBySession");
            data.add("price");
            data.add("cat");
            data.add("buyCount");
            w.write(Joiner.on(",").join(data) + "\n");

            for (String pid : pidViewCount.counter.keySet()) {
                data.clear();

                data.add(pid);
                data.add("" + pidViewCount.getValueOrZero(pid));
                data.add("" + pidUniqueViewCount.getValue(pid).size());
                Integer price = NetworkPriceFetcher.lookPrice(pid);
                if (price == null) {
                    System.err.println("skip " + pid);
                    continue;
                }
                data.add("" + price);
                String theCat = catMap.get(pid);
                int asciiCode = theCat.charAt(0);
                data.add("" + (asciiCode)); // lm regression 需要數字
                data.add("" + orderCount.getValueOrZero(pid));
                w.write(Joiner.on(",").join(data) + "\n");
            }

            NetworkPriceFetcher.savePriceState();
        }

        public void order(String line) {
            String s = StringUtils.substringBetween(line, "plist=", ";");
            try {
                String[] ss = s.split(",");
                String pid = ss[0];
                int num = NumberUtils.toInt(ss[1], 1);
                String price = ss[2];
                if (NumberUtils.isNumber(price)) {
                    NetworkPriceFetcher.update(pid, price);
                }
                orderCount.count(pid, num);
            } catch (Exception e) {
            }

        }

    }

}
