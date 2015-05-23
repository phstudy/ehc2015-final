package org.qty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ord.phstudy.ehc.PriceUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import file.FileManager;

public class Lab3 {

    private static final String NO_PID = "000000";

    static String eruid(String line) {
        String s = StringUtils.substringBetween(line, "erUid=", ";");
        return Optional.fromNullable(s).or("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
    }

    static String pid(String line) {
        String s = StringUtils.substringBetween(line, "pid=", ";");
        return Optional.fromNullable(s).or(NO_PID);
    }

    // 純粹以 click history 來看，先刪除 history 小於 100 與大於 2000 者，保留最後 400 筆的結果. 
    //    
    //    qty:preprocessing qrtt1$ ./build/install/preprocessing/bin/preprocessing 100 2000 400
    //    pruneLessThanThreshold: 100, pruneGreaterThanThreshold: 2000, keepLastN: 400
    //    reset base-dir as /Users/qrtt1/Desktop/EHC
    //    elapsed [1] => 7 seconds.
    //    elapsed [2] => 15 seconds.
    //    pid num: 223686
    //    compare with top [20] 8
    //    compare with top [40] 10
    //    compare with top [60] 11
    //    compare with top [80] 11
    //    compare with top [100] 11
    //    compare with top [1000] 13
    //    compare with top [3000] 14
    //    elapsed [3] => 16 seconds.
    //    close java.io.BufferedReader@30f39991
    //    close java.io.BufferedReader@38af3868

    public static void main(String[] args) throws Exception {

        int pruneGreaterThanThreshold = 500;
        int pruneLessThanThreshold = 0;
        int keepLastN = 1000;

        if (args.length == 3) {
            pruneLessThanThreshold = NumberUtils.toInt(args[0]);
            pruneGreaterThanThreshold = NumberUtils.toInt(args[1]);
            keepLastN = NumberUtils.toInt(args[2]);
        }

        System.out.println(String.format("pruneLessThanThreshold: %s, pruneGreaterThanThreshold: %s, keepLastN: %s",
                pruneLessThanThreshold, pruneGreaterThanThreshold, keepLastN));
        Counter viewCounter = new Counter();

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s : FileManager.fileAsLineIterator(inputFile())) {
            String eruid = eruid(s);
            if (eruid.contains(" ")) {
                continue;
            }
            if (!s.contains("act=v")) {
                continue;
            }
            viewCounter.count(eruid);
        }

        // 去掉 access 太多的 eruid
        viewCounter.filterOutCountGreaterThan(pruneGreaterThanThreshold);
        viewCounter.filterOutCountLessThan(pruneLessThanThreshold);
        System.out.println("elapsed [1] => " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");

        Counter pidCounter = new Counter();
        ClickHistory clickHistory = new ClickHistory(keepLastN);
        for (String s : FileManager.fileAsLineIterator(inputFile())) {
            String eruid = eruid(s);
            if (eruid.contains(" ")) {
                continue;
            }
            if (!s.contains("act=v")) {
                continue;
            }

            pidCounter.count(pid(s));
            if (viewCounter.containsKey(eruid)) {
                clickHistory.click(eruid, pid(s));
            }
        }

        System.out.println("elapsed [2] => " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
        System.out.println("pid num: " + pidCounter.size());
        clickHistory.write();
        System.out.println("elapsed [3] => " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");

    }

    protected static String inputFile() {
        return "EHC_2nd_round_train.log";
    }

    static class Counter {

        Map<String, AtomicInteger> counter = new HashMap<String, AtomicInteger>();

        public Set<String> key() {
            return counter.keySet();
        }

        public void filterOutCountLessThan(int threshold) {
            Iterator<Entry<String, AtomicInteger>> it = counter.entrySet().iterator();
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
            Iterator<Entry<String, AtomicInteger>> it = counter.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().intValue() > threshold) {
                    it.remove();
                }
            }
        }

        public int size() {
            return counter.size();
        }

        public void count(String s) {
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
    }

    static class ClickHistory {

        private int keepLastN;
        private Map<String, Queue<String>> store = new HashMap<String, Queue<String>>();

        public ClickHistory(int keepLastN) {
            this.keepLastN = keepLastN;
        }

        public void click(String eruid, String pid) {
            if (store.containsKey(eruid)) {
                Queue<String> queue = store.get(eruid);
                queue.add(pid);
                while (queue.size() > keepLastN) {
                    queue.poll();
                }
                return;
            }

            Queue<String> queue = new LinkedList<String>();
            queue.add(pid);
            store.put(eruid, queue);
        }

        public void write() throws IOException {
            Counter counter = new Counter();
            Iterator<Entry<String, Queue<String>>> it = store.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Queue<String>> e = it.next();
                it.remove();
                //                Queue<String> client = e.getValue();
                Set<String> client = new HashSet<String>(e.getValue());
                for (String pid : client) {
                    counter.count(pid);
                }
            }

            ArrayList<Entry<String, AtomicInteger>> list = new ArrayList<Map.Entry<String, AtomicInteger>>(
                    counter.counter.entrySet());

            // 把數量轉成金額
            for (Entry<String, AtomicInteger> entry : list) {
                Integer v = PriceUtils.prices.get(entry.getKey());
                if (v == null) {
                    continue;
                }
                entry.getValue().set(entry.getValue().intValue() * v);
            }

            Collections.sort(list, new Comparator<Entry<String, AtomicInteger>>() {

                @Override
                public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
                    return o2.getValue().intValue() - o1.getValue().intValue();
                }
            });

            show(list, 20);
            show(list, 40);
            show(list, 60);
            show(list, 80);
            show(list, 100);
            show(list, 1000);
            show(list, 3000);
            show(list, 10000);
        }

        protected void show(ArrayList<Entry<String, AtomicInteger>> list, int topN) throws IOException {
            Set<String> output = Sets.newHashSet();
            for (Entry<String, AtomicInteger> e : list.subList(0, topN)) {
                output.add(e.getKey());
            }

            SetView<String> view = Sets.intersection(Answer.getPidSet(), Sets.newHashSet(output));
            System.out.println("compare with top [" + topN + "] " + view.size());
        }

    }
}
