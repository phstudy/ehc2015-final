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

import org.apache.commons.lang3.StringUtils;
import org.phstudy.ehc.utils.PriceUtils;
import org.qty.ItemCounter;
import org.qty.QLabInitConfig;
import org.qty.file.FileManager;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ReadAndCheck_V3_BuyOneRandomWithPrice {

    public static void main(String[] args) throws Exception {
        String resultFile = "model_1_result.csv";
        Set<String> buyUserEruids = FileManager.readPredictEruidsResult(resultFile);
        UserItemSet userItemSet = new UserItemSet();

        for (String s : FileManager.fileAsLineIterator(QLabInitConfig.TEST_FILE)) {

            String eruid = ValidationUtils.eruid(s);
            String pid = ValidationUtils.pid(s);

            if (!s.contains("act=v")) {
                continue;
            }

            if (!buyUserEruids.contains(eruid)) {
                continue;
            }
            userItemSet.addItem(eruid, pid);
        }

        ItemCounter<String> count = new ItemCounter<String>();

        int countDown = 0;

        for (Entry<String, List<String>> s : userItemSet.toList()) {
            // 隨機挑 1 個
            List<String> viewList = s.getValue();
            Collections.shuffle(viewList);
            String pid = viewList.get(0);
            Integer price = NetworkPriceFetcher.lookPrice(pid);
            if (price == null) {
                price = 1;
                System.err.println("no price: " + pid);
            } else {
            }
            count.count(pid, price);

            countDown++;
            //            if (countDown % 1000 == 0) {
            //                NetworkPriceFetcher.savePriceState();
            //                System.out.println("save state");System.out.println("save state");System.out.println("save state");
            //            }
        }

        showResult(count, 20);
        showResult(count, 200);
        showResult(count, 2000);

        NetworkPriceFetcher.savePriceState();
    }

    protected static void showResult(ItemCounter<String> count, int topN) {
        Set<String> predict = Sets.newHashSet();
        for (Entry<String, AtomicInteger> item : count.getTopN(topN)) {
            predict.add(item.getKey());
        }

        //                System.out.println(predict);
        //                System.out.println(TestAnswer.ANSWER_PIDS);
        System.out.println("top" + topN + " => " + Sets.intersection(predict, TestAnswer.ANSWER_PIDS).size());
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
