package org.qty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

public class ItemCounter<Key> {

    Map<Key, AtomicInteger> counter = new HashMap<Key, AtomicInteger>();

    public Set<Key> key() {
        return counter.keySet();
    }

    /**
     * 去掉 count 少於 threshold 的 item
     * 
     * @param threshold
     */
    public void filterOutCountLessThan(int threshold) {
        Iterator<Entry<Key, AtomicInteger>> it = counter.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().intValue() < threshold) {
                it.remove();
            }
        }
    }

    /**
     * 去掉 count 超過 threshold 的 item
     * 
     * @param threshold
     */
    public void filterOutCountGreaterThan(int threshold) {
        Iterator<Entry<Key, AtomicInteger>> it = counter.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().intValue() > threshold) {
                it.remove();
            }
        }
    }

    public void filterOutCountOutsideTheRange(int lowBound, int highBound) {
        Iterator<Entry<Key, AtomicInteger>> it = counter.entrySet().iterator();
        while (it.hasNext()) {
            int v = it.next().getValue().intValue();
            if (v < lowBound || v > highBound) {
                it.remove();
            }
        }
    }

    public int size() {
        return counter.size();
    }

    public void count(Key s) {
        if (counter.containsKey(s)) {
            counter.get(s).incrementAndGet();
            return;
        }
        counter.put(s, new AtomicInteger(1));
    }

    public void count(Key s, int weight) {
        if (counter.containsKey(s)) {
            int origin = counter.get(s).get();
            counter.get(s).set(origin + weight);
            return;
        }
        counter.put(s, new AtomicInteger(weight));
    }

    public boolean containsKey(Key s) {
        return counter.containsKey(s);
    }

    public String ratio(String s, double base) {
        if (counter.containsKey(s)) {
            double v = counter.get(s).doubleValue() / base;
            return String.format("%.10f", v);
        }
        return "0";
    }

    @Override
    public String toString() {
        return "" + counter;
    }

    public int getValue(Key key) {
        return counter.get(key).intValue();
    }

    public int getValueOrZero(Key key) {
        try {
            return counter.get(key).intValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public void setValue(Key key, int newValue) {
        counter.get(key).set(newValue);
    }

    public List<Entry<Key, AtomicInteger>> getTopN(int n) {
        ArrayList<Entry<Key, AtomicInteger>> list = Lists.newArrayList(counter.entrySet());
        Collections.sort(list, new Comparator<Entry<Key, AtomicInteger>>() {
            @Override
            public int compare(Entry<Key, AtomicInteger> o1, Entry<Key, AtomicInteger> o2) {
                return o2.getValue().intValue() - o1.getValue().intValue();
            }
        });

        return list.subList(0, n);
    }

    public Set<Entry<Key, AtomicInteger>> entrySet() {
        return counter.entrySet();
    }

}
