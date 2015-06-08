package org.qty.validate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.qty.ItemCounter;
import org.qty.file.FileManager;
import org.qty.validate.GaImpl.BuyCountChromosome;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ReadAndCheck_V6_BuyByPredictBuyCountGA {

    public static void main(String[] args) throws Exception {
        String logdata = args[0];
        String usermodel = args[1];
        String buymodel = args[2];
        float threshold = Float.valueOf(args[3]);
        String outputFile = args[4];

        ProductBuyManager buyManager = new ProductBuyManager(buymodel, threshold);

        Set<String> buyUserEruids = FileManager.readPredictEruidsResult(usermodel);
        UserItemSet userItemSet = new UserItemSet();

        for (String s : FileManager.fileAsLineIterator(logdata)) {

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

        Set<String> orderPids = new HashSet<String>();
        for (Entry<String, List<String>> s : userItemSet.toList()) {
            List<String> viewList = s.getValue();
            orderPids.addAll(viewList);
            for (String pid : viewList) {
                if (buyManager.buyIt(pid)) {
                    buy(count, pid);
                }
            }
        }

        System.out.println("[data from product model] predict total buy count: " + buyManager.getTotalCount());
        System.out.println("[data from product model] predict total pid count: " + buyManager.pidCount.keySet().size());

        System.out.println("[data from order model] predict order count: " + buyUserEruids.size());
        System.out.println("[data from order model] predict pid count: " + orderPids.size());
        System.out.println("real buy count: " + count.size());

        if (showResult(count, count.size()) != 16) {
            System.err.println("ERR");
            System.err.println("ERR");
            System.err.println("ERR");
            System.err.println("ERR");
            System.err.println("ERR");
            System.exit(0);
        }

        int bound = 100;
        while (true) {
            if (showResult(count, bound) == 16) {
                break;
            }
            bound += 50;
        }

        Set<String> smallList = new HashSet<String>();
        for (Entry<String, AtomicInteger> e : count.getTopN(bound)) {
            smallList.add(e.getKey());
        }

        // 取 order predict pids、product predict pids 與 price pids 的交集 
        Set<String> intersection = Sets.intersection(buyManager.pidCount.keySet(), orderPids);
        intersection = Sets.intersection(intersection, smallList);

        Map<String, Integer> priceSubset = NetworkPriceFetcher.buildPriceSet(intersection);
        intersection = Sets.intersection(intersection, priceSubset.keySet());
        System.out.println("[ga] intersection size: " + intersection.size());
        System.out.println("[ga] price size: " + priceSubset.size());

        BuyCountChromosome chromosome = runGA(buyManager, priceSubset);

        showResult(chromosome.itemCounter, 20);
        showResult(chromosome.itemCounter, 35);
        showResult(chromosome.itemCounter, 50);
        showResult(chromosome.itemCounter, 100);
        showResult(chromosome.itemCounter, 200);
        showResult(chromosome.itemCounter, intersection.size());

        Writer out = FileManager.fileAsWriter(outputFile);
        int rankNumber = 1;
        for (Entry<String, AtomicInteger> e : chromosome.itemCounter.getTopN(20)) {
            //            out.write(e.getKey() + "," + e.getValue().intValue() + "\n");
            out.write(String.format("%02d,%s\n", rankNumber, e.getKey()));
            System.out.println(String.format("%02d,%s,%s", rankNumber, e.getKey(), e.getValue()));
            rankNumber++;
        }

        NetworkPriceFetcher.savePriceState();
    }

    protected static BuyCountChromosome runGA(ProductBuyManager buyManager, Map<String, Integer> priceSubset) {
        GaImpl gg = new GaImpl(priceSubset, TestAnswer.ANSWER_PIDS, buyManager.pidWeight);
        while (true) {
            try {
                return gg.evolve();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

    protected static void buy(ItemCounter<String> count, String pid) throws FileNotFoundException, IOException {
        Integer price = NetworkPriceFetcher.lookPrice(pid);
        if (price == null) {
            price = 1;
            System.err.println("no price: " + pid);
        } else {
        }
        count.count(pid, price);
    }

    protected static int showResult(ItemCounter<String> count, int topN) {
        Set<String> predict = Sets.newHashSet();
        for (Entry<String, AtomicInteger> item : count.getTopN(topN)) {
            predict.add(item.getKey());
        }
        int inTop = Sets.intersection(predict, TestAnswer.ANSWER_PIDS).size();
        System.out.println("top" + topN + " => " + inTop);
        return inTop;
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
