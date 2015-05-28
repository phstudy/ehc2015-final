package org.phstudy.ehc.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.phstudy.ehc.tools.FillMissingCid.Category;
import org.qty.file.FileManager;

/**
 * Created by study on 5/28/15.
 */
public class FillMissingCid {
    public static void main(String[] args) throws Exception {
        BufferedReader c1Br = FileManager.fileAsReader("all_category.csv");
        BufferedReader noclassBr = FileManager.fileAsReader("noclass.csv");

        Set<Long> pidset = new HashSet<Long>(200000);

        List<Long> pids = new ArrayList<Long>(200000);
        Map<Long, Category> pidMapping = new HashMap<Long, Category>(200000);

        BufferedWriter cWb = FileManager.fileAsWriter("all_category2.csv");

        AtomicInteger cnt = new AtomicInteger(0);
        String line;
        while ((line = c1Br.readLine()) != null) {
            try {
                cnt.incrementAndGet();
                if (cnt.intValue() % 10000 == 0) {
                    System.out.println(cnt.intValue());
                }

                String[] str = line.split(",");
                String pid = str[0];


                int price = Integer.parseInt(str[1]);
                String cid = str[2];

                Long intPid = Long.parseLong(pid);
                if (!pidset.contains(intPid)) {
                    pidset.add(intPid);
                    pids.add(intPid);
                    pidMapping.put(intPid, new Category(pid, price, cid));
                }
            } catch (Exception e) {
                System.out.println(line);
            }
        }
        pidset.clear();

        AtomicInteger cnt2 = new AtomicInteger(0);

        while ((line = noclassBr.readLine()) != null) {
            try {
                cnt2.incrementAndGet();
                if (cnt2.intValue() % 10000 == 0) {
                    System.out.println(cnt2.intValue());
                }

                String str[] = line.split(",");
                String pidStr = str[0];
                if (str[0].contains("_")) {
                    str[0] = str[0].split("_")[0];
                }
                long pid = Long.parseLong(str[0]);
                int price = Integer.parseInt(str[1]);

                int idx = 0;
                int index = Collections.binarySearch(pids, pid);
                if (index < 0) {
                    int idxPreviousDate = Math.max(0, -index - 2);
                    long previousDate = pids.get(idxPreviousDate);
                    int idxnextDate = Math.min(pids.size() - 1, -index - 1);
                    long nextDate = pids.get(idxnextDate);
                    idx = pid - previousDate < nextDate - pid ? idxPreviousDate : idxnextDate;
                } else {
                    idx = index;
                }

                int min = Integer.MAX_VALUE;
                String cid = "";
                for (int i = idx - 1; i >= 0 && i <= idx + 1 && i < pids.size(); i++) {
                    long intPid = pids.get(idx);
                    Category c = pidMapping.get(intPid);
                    int val = Math.abs(c.price - price);
                    if (val < min) {
                        min = val;
                        cid = c.cid;
                    }
                }

                cWb.write(pidStr + "," + price + "," + cid + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        cWb.flush();
        cWb.close();
    }


    public static class Category {
        public Category(String pid, int price, String cid) {
            this.pid = pid;
            this.price = price;
            this.cid = cid;
        }

        public String pid;
        public int price;
        public String cid;
    }
}
