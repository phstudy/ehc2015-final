package org.qty.validate;

import static org.qty.QLabInitConfig.NO_PID;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.qty.ItemCounter;
import org.qty.QLabInitConfig;
import org.qty.file.FileManager;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class ReadAndCheck {
    static String eruid(String line) {
        String s = StringUtils.substringBetween(line, "erUid=", ";");
        return Optional.fromNullable(s).or("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
    }

    static String pid(String line) {
        String s = StringUtils.substringBetween(line, "pid=", ";");
        return Optional.fromNullable(s).or(NO_PID);
    }

    public static void main(String[] args) throws Exception {
        String resultFile = "model_1_result.csv";
        Set<String> buyUserEruids = FileManager.readPredictEruidsResult(resultFile);
        UserItemSet userItemSet = new UserItemSet();

        for (String s : FileManager.fileAsLineIterator(QLabInitConfig.TEST_FILE)) {

            String eruid = eruid(s);
            String pid = pid(s);

            if (!s.contains("act=v")) {
                continue;
            }

            if (!buyUserEruids.contains(eruid)) {
                continue;
            }
            userItemSet.addItem(eruid, pid);
        }

        ItemCounter<String> count = new ItemCounter<String>();

        for (Entry<String, List<String>> s : userItemSet.toList()) {
            for (String pid : s.getValue()) {
                count.count(pid);
            }
        }

        for (Entry<String, AtomicInteger> item : count.getTopN(10)) {
            System.out.println(item);
        }

    }

    static class UserItemSet {
        Map<String, Set<String>> userPids = new HashMap<String, Set<String>>();

        public void addItem(String eruid, String pid) {
            if (!userPids.containsKey(eruid)) {
                userPids.put(eruid, new HashSet<String>());
            }
            userPids.get(eruid).add(pid);
        }

        public List<Entry<String, List<String>>> toList() {
            List<Entry<String, List<String>>> l = Lists.newArrayList();

            for (final Entry<String, Set<String>> o : userPids.entrySet()) {
                l.add(new Entry<String, List<String>>() {

                    @Override
                    public List<String> setValue(List<String> value) {
                        return null;
                    }

                    @Override
                    public List<String> getValue() {
                        return Lists.newArrayList(o.getValue());
                    }

                    @Override
                    public String getKey() {
                        return o.getKey();
                    }

                    @Override
                    public String toString() {
                        return String.format("%s => %s", o.getKey(), o.getValue());
                    }
                });
            }

            Collections.sort(l, new Comparator<Entry<String, List<String>>>() {

                @Override
                public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {
                    return o1.getValue().size() - o2.getValue().size();
                }
            });
            return l;
        }
    }
}
