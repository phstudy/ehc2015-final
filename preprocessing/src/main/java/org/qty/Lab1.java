package org.qty;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ord.phstudy.ehc.ExtractorUtils;

import com.google.common.base.Stopwatch;

import file.FileManager;

public class Lab1 {

    public static void main(String[] args) throws Exception {

        long viewCount = 0;
        long searchCount = 0;
        long totalCount = 0;

        Counter viewCounter = new Counter();
        Counter searchCounter = new Counter();

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s : FileManager.fileAsLineIterator("EHC_2nd_round_test.log")) {
            String eruid = ExtractorUtils.extractEruid(s);
            if (s.contains("act=v")) {
                viewCount++;
                totalCount++;
                viewCounter.count(eruid);
            }
            if (s.contains("act=s")) {
                searchCount++;
                totalCount++;
                searchCounter.count(eruid);
            }
        }

        System.out.println(viewCount);
        System.out.println(searchCount);
        System.out.println(totalCount);

        System.out.println("elapsed at counting " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
        //        System.out.println(searchCounter);
        //        System.out.println(viewCounter);

        TreeSet<String> eruids = new TreeSet<String>();
        eruids.addAll(viewCounter.key());
        eruids.addAll(searchCounter.key());
        System.out.println(eruids.size());

        Writer w = FileManager.fileAsWriter("qtylab.out");
        StringBuilder sb = new StringBuilder();
        for (String s : eruids) {
            sb.setLength(0);
            sb.append(s).append(",");
            sb.append(viewCounter.ratio(s, viewCount)).append(",");
            sb.append(searchCounter.ratio(s, searchCount)).append("\n");
            w.write(sb.toString());
        }
        System.out.println("elapsed at writing " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");

    }

    static class Counter {

        Map<String, AtomicInteger> counter = new HashMap<String, AtomicInteger>();

        public Set<String> key() {
            return counter.keySet();
        }

        public void count(String s) {
            if (counter.containsKey(s)) {
                counter.get(s).incrementAndGet();
                return;
            }
            counter.put(s, new AtomicInteger(1));
        }

        public double ratio(String s, double base) {
            if (counter.containsKey(s)) {
                return counter.get(s).doubleValue() / base;
            }
            return 0D;
        }

        @Override
        public String toString() {
            return "" + counter;
        }
    }
}
