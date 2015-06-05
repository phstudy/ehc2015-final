package org.qty.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.math.NumberUtils;
import org.qty.file.FileManager;

public class ProductBuyManager {

    Map<String, AtomicInteger> pidCount = new HashMap<String, AtomicInteger>();
    Map<String, Float> pidWeight = new HashMap<String, Float>();

    public ProductBuyManager(String filename, float threshold) throws Exception {
        init(filename, threshold);
    }

    private void init(String filename, float threshold) throws Exception {
        for (String[] data : FileManager.fileAsCSVRowIterator(filename)) {
            //            System.out.println(data[0] + " => " + data[1]);
            float originValue = NumberUtils.toFloat(data[1]);
            pidWeight.put(data[0], originValue);
            if (originValue < threshold) {
                continue;
            }

            pidCount.put(data[0], new AtomicInteger((int) Math.round(originValue + 0.5)));
        }

        List<Entry<String, AtomicInteger>> lll = new ArrayList<Map.Entry<String, AtomicInteger>>(pidCount.entrySet());
        Collections.sort(lll, new Comparator<Entry<String, AtomicInteger>>() {

            @Override
            public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
                return o1.getValue().intValue() - o2.getValue().intValue();
            }
        });

        for (Entry<String, AtomicInteger> x : lll) {
            System.out.println(x.getKey() + " => " + x.getValue());
        }

        long sum = 0;
        for (AtomicInteger i : pidCount.values()) {
            sum += i.intValue();
        }
        System.out.println("sum: " + sum);
    }

    public float getWeight(String pid) {
        if (pidWeight.containsKey(pid)) {
            return pidWeight.get(pid);
        }
        return 0.01F;
    }

    public boolean buyIt(String pid) {
        if (!pidCount.containsKey(pid)) {
            return false;
        }

        if (pidCount.get(pid).intValue() > 1) {
            pidCount.get(pid).decrementAndGet();
            return true;
        }
        return false;
    }

}
