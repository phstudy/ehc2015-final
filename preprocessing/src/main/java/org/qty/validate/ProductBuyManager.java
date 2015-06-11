package org.qty.validate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.math.NumberUtils;
import org.qty.file.FileManager;

public class ProductBuyManager {

    Map<String, AtomicInteger> pidCount = new HashMap<String, AtomicInteger>();
    Map<String, Float> pidWeight = new HashMap<String, Float>();
    private long totalCount;

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

        long sum = 0;
        for (AtomicInteger i : pidCount.values()) {
            sum += i.intValue();
        }
        totalCount = sum;
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

    public long getTotalCount() {
        return totalCount;
    }

}
