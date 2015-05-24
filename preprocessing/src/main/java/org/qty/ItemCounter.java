package org.qty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemCounter<Key> {

    Map<Key, AtomicInteger> counter = new HashMap<Key, AtomicInteger>();

    public Set<Key> key() {
        return counter.keySet();
    }

    public void filterOutCountLessThan(int threshold) {
        Iterator<Entry<Key, AtomicInteger>> it = counter.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().intValue() < threshold) {
                it.remove();
            }
        }
    }

    public boolean containsKey(String eruid) {
        return counter.containsKey(eruid);
    }

    public void filterOutCountGreaterThan(int threshold) {
        Iterator<Entry<Key, AtomicInteger>> it = counter.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().intValue() > threshold) {
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

    public void setValue(Key key, int newValue) {
        counter.get(key).set(newValue);
    }
}
