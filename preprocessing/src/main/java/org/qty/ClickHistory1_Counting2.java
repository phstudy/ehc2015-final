package org.qty;

import static org.qty.QLabInitConfig.INPUT_FILE;
import static org.qty.QLabInitConfig.NO_PID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.qty.file.FileManager;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class ClickHistory1_Counting2 {

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

        int highBound = 5000;
        int lowBound = 0;

        if (args.length == 3) {
            lowBound = NumberUtils.toInt(args[0]);
            highBound = NumberUtils.toInt(args[1]);
        }

        System.out.println(String.format("pruneLessThanThreshold: %s, pruneGreaterThanThreshold: %s", lowBound,
                highBound));

        ItemCounter<String> viewCounter = new ItemCounter<String>();

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s : FileManager.fileAsLineIterator(INPUT_FILE)) {
            String eruid = eruid(s);
            if (eruid.contains(" ")) {
                continue;
            }
            if (!s.contains("act=v")) {
                continue;
            }
            viewCounter.count(eruid);
        }

        // 去掉 access 不在範圍內的 eruid
        viewCounter.filterOutCountOutsideTheRange(lowBound, highBound);

        System.out.println("elapsed [1] => " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");

        ItemCounter<String> pidCounter = new ItemCounter<String>();
        ClickHistory clickHistory = new ClickHistory(0);
        for (String s : FileManager.fileAsLineIterator(INPUT_FILE)) {
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

    static class ClickHistory {

        private Map<String, Set<String>> store = new HashMap<String, Set<String>>();

        public ClickHistory(int keepLastN) {
        }

        public void click(String eruid, String pid) {
            if (store.containsKey(eruid)) {

                return;
            }

            Set<String> set = new HashSet<String>();
            set.add(pid);
            store.put(eruid, set);
        }

        public void write() throws IOException {
            ItemCounter<String> counter = new ItemCounter<String>();
            Iterator<Entry<String, Set<String>>> it = store.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, Set<String>> e = it.next();
                it.remove();
                for (String s : e.getValue()) {
                    counter.count(s);
                }
            }

            ArrayList<Entry<String, AtomicInteger>> list = new ArrayList<Map.Entry<String, AtomicInteger>>(
                    counter.counter.entrySet());
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
