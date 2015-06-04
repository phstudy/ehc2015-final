package org.qty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemGroupCounter<Key, Group> {

    Map<Key, Set<Group>> counter = new HashMap<Key, Set<Group>>();

    public Set<Key> key() {
        return counter.keySet();
    }

    public int size() {
        return counter.size();
    }

    public void count(Key s, Group group) {
        if (counter.containsKey(s)) {
            counter.get(s).add(group);
            return;
        }
        counter.put(s, new HashSet<Group>());
        counter.get(s).add(group);
    }

    public boolean containsKey(Key s) {
        return counter.containsKey(s);
    }

    @Override
    public String toString() {
        return "" + counter;
    }

    public Set<Group> getValue(Key key) {
        return counter.get(key);
    }

}
