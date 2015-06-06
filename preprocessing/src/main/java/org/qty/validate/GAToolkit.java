package org.qty.validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Random;
import java.util.Set;

import org.qty.ItemCounter;

import com.google.common.collect.Lists;

public class GAToolkit {

    int minCost = Integer.MAX_VALUE;
    int maxPopulations = 6;
    Random random = new Random();

    Map<String, Integer> priceMap;
    Set<String> answerSet;
    Map<String, Float> pidWeight;

    ArrayList<int[]> populations = new ArrayList<int[]>();

    public GAToolkit(Map<String, Integer> priceMap, Set<String> answerSet, Map<String, Float> pidWeight) {
        this.priceMap = priceMap;
        this.answerSet = answerSet;
        this.pidWeight = pidWeight;

        while (populations.size() < maxPopulations) {
            addRandomPopulation();
        }
    }

    private void addRandomPopulation() {
        int[] p = new int[priceMap.keySet().size()];
        for (int i = 0; i < p.length; i++) {
            p[i] = Math.abs(random.nextInt(10000));
        }
        this.populations.add(p);
    }

    public void go() {
        runIterator();
    }

    protected void runIterator() {
        dropBadPopulation();

        int retry = 0;
        while (true) {
            if (retry > 100) {
                addRandomPopulation();
                mutation();
                break;
            }
            int[] newP = newPropulationByCrossover();
            int pCost = getCost(newP);
            boolean conflict = false;
            for (int[] p : populations) {
                int cost = getCost(p);
                if (cost == pCost) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict) {
                populations.add(newP);
                break;
            }
            retry++;
        }

        if (minCost < 1000) {
            System.exit(0);
            return;
        }

        mutation();
    }

    private int[] newPropulationByCrossover() {

        int index1 = 0;
        int index2 = 0;
        while (index1 == index2) {
            index1 = Math.abs(random.nextInt() % populations.size());
            index2 = Math.abs(random.nextInt() % populations.size());
        }

        int[] select1 = populations.get(index1);
        int[] select2 = populations.get(index2);

        int crossoverPoint = Math.abs(random.nextInt()) % select1.length;
        while (crossoverPoint < 2) {
            crossoverPoint = Math.abs(random.nextInt()) % select1.length;
        }

        int[] newPopulation = new int[select1.length];

        for (int i = 0; i < newPopulation.length; i++) {
            if (i < crossoverPoint) {
                newPopulation[i] = select1[i];
            } else {
                newPopulation[i] = select2[i];
            }
        }

        Set<Integer> costSet = new HashSet<Integer>();
        for (int[] p : populations) {
            costSet.add(getCost(p));
        }

        return newPopulation;
    }

    protected void dropBadPopulation() {
        if (populations.size() < 2) {
            return;
        }

        int index = 0;
        int maxCost = 0;
        int maxCostIndex = 0;

        StringBuffer sb = new StringBuffer();
        for (int[] p : populations) {
            int cost = getCost(p);
            if (cost < minCost) {
                minCost = cost;
            }

            if (cost > maxCost) {
                maxCost = cost;
                maxCostIndex = index;
            }
            sb.append("cost index[" + index + "]=").append(cost).append(", ");
            index++;
        }
        sb.append(" drop maxCost at " + maxCostIndex);
        System.out.println("[GA] " + sb);
        populations.remove(maxCostIndex);
    }

    private void mutation() {
        int numOfmutate = Math.abs(random.nextInt(populations.size()));
        for (int i = 0; i < numOfmutate; i++) {
            int select = Math.abs(random.nextInt()) % populations.size();
            int[] p = populations.get(select);
            int index = Math.abs(random.nextInt()) % p.length;
            int value = Math.abs(random.nextInt(5));
            boolean addOrSubtrace = random.nextBoolean();
            if (p[index] > 500) {
                i--;
                continue;
            }
            if (addOrSubtrace) {
                p[index] += value;
            } else {
                p[index] -= value;
                if (p[index] < 1) {
                    p[index] = 1;
                }
            }

        }

    }

    /**
     * cost 簡單計算各別應該進榜的 pid 離 top 20 的距離的總合，小於 20 以內算 0 距離
     * 
     * @param buyCounts
     */
    private int getCost(int[] buyCounts) {
        while (true) {
            try {
                return getCostWithoutExecption(buyCounts);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    protected int getCostWithoutExecption(int[] buyCounts) {
        int index = 0;
        ItemCounter<String> c = new ItemCounter<String>();
        for (Entry<String, Integer> priceEntry : priceMap.entrySet()) {
            int count = buyCounts[index++];
            c.count(priceEntry.getKey(), count * priceEntry.getValue());
        }

        List<String> ranking = toOrderedKeys(c);

        int cost = 0;
        for (String s : answerSet) {
            int offset = ranking.indexOf(s);
            if (offset == -1) {
                cost += 10000;
            } else if (offset <= 20) {
                cost += 0;
            } else {
                cost += (offset - 20);
            }
        }
        return cost;
    }

    public List<String> toOrderedKeys(ItemCounter<String> c) {
        ArrayList<Entry<String, AtomicInteger>> list = Lists.newArrayList(c.entrySet());
        Collections.sort(list, new Comparator<Entry<String, AtomicInteger>>() {
            @Override
            public int compare(Entry<String, AtomicInteger> o1, Entry<String, AtomicInteger> o2) {
                if (o2.getValue().intValue() == o1.getValue().intValue()) {
                    return 0;
                }
                return o2.getValue().intValue() > o1.getValue().intValue() ? 1 : -1;
            }
        });

        ArrayList<String> ss = new ArrayList<String>();
        for (Entry<String, AtomicInteger> e : list) {
            ss.add(e.getKey());
        }
        return ss;
    }

}
