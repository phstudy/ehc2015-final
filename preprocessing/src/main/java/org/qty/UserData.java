package org.qty;

import static org.qty.QLabInitConfig.NO_PID;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.qty.file.FileManager;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

public class UserData {

    static String eruid(String line) {
        String s = StringUtils.substringBetween(line, "erUid=", ";");
        return Optional.fromNullable(s).or("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx");
    }

    static String pid(String line) {
        String s = StringUtils.substringBetween(line, "pid=", ";");
        return Optional.fromNullable(s).or(NO_PID);
    }

    static int numOfPids(String line) {
        String s = StringUtils.substringBetween(line, "plist=", ";");
        String[] ss = s.split(",");
        return ss.length % 3 == 0 ? ss.length / 3 : 0;
    }

    static String mainCategory(String line) {
        try {
            String s = StringUtils.substringBetween(line, "cat=", ";");
            return Optional.fromNullable("" + s.charAt(0)).or("_");
        } catch (Exception e) {
            return "_";
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("<cmd> <inputFile> <outputFile>");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];

        UserManager manager = new UserManager();
        for (String s : FileManager.fileAsLineIterator(inputFile)) {

            String eruid = eruid(s);
            String pid = pid(s);

            if (eruid.contains(" ")) {
                continue;
            }

            if (s.contains("act=o")) {
                manager.order(eruid, numOfPids(s));
                continue;
            }

            if (!s.contains("act=v")) {
                continue;
            }

            String cat = mainCategory(s);
            if ("_".equals(cat)) {
                continue;
            }
            //            System.out.println(pid+"_" + cat);
            manager.viewAction(eruid, pid, cat);

        }

        manager.dump(outputFile);
    }

    static class UserManager {

        Map<String, ItemCounter<String>> userViewCount = new HashMap<String, ItemCounter<String>>();
        Map<String, Set<String>> userUniqueViewCount = new HashMap<String, Set<String>>();

        Map<String, ItemCounter<String>> userCatCount = new HashMap<String, ItemCounter<String>>();
        Map<String, Set<String>> userUniqueCatCount = new HashMap<String, Set<String>>();

        Set<String> orderMarkers = new HashSet<String>();
        ItemCounter<String> buyCount = new ItemCounter<String>();

        public void viewAction(String eruid, String pid, String cat) {
            if (!userViewCount.containsKey(eruid)) {
                userViewCount.put(eruid, new ItemCounter<String>());
                userUniqueViewCount.put(eruid, new HashSet<String>());

                userCatCount.put(eruid, new ItemCounter<String>());
                userUniqueCatCount.put(eruid, new HashSet<String>());
            }

            userViewCount.get(eruid).count(pid);
            userUniqueViewCount.get(eruid).add(pid);

            userCatCount.get(eruid).count(cat);
            userUniqueCatCount.get(eruid).add(cat);
        }

        public void order(String eruid, int pidlist) {
            orderMarkers.add(eruid);
            buyCount.count(eruid);
        }

        public void dump(String filename) throws Exception {

            //            Writer w = FileManager.fileAsWriter(filename);
            Writer ew = FileManager.fileAsWriter(filename);

            List<String> allCates = getCategoryList();
            ArrayList<Object> output = new ArrayList<Object>();
            ArrayList<Object> erOutput = new ArrayList<Object>();

            output.add("viewcount");
            output.add("uniq_viewcount");
            for (String c : allCates) {
                output.add("cat_" + c);
            }
            output.add("max_cat");
            output.add("buyCount");
            output.add("buy");

            erOutput.addAll(output);
            erOutput.add(0, "eruid");
            //            w.write(Joiner.on(",").join(output) + "\n");
            ew.write(Joiner.on(",").join(erOutput) + "\n");

            //
            //

            for (String user : userViewCount.keySet()) {
                output.clear();
                erOutput.clear();

                long viewCount = 0;
                for (AtomicInteger v : userViewCount.get(user).counter.values()) {
                    viewCount += v.intValue();
                }
                // view count
                output.add(viewCount);

                // unique view count
                output.add(userUniqueViewCount.get(user).size());

                // cates
                int maxCate = 0;
                Object maxCateName = "_";

                for (String c : allCates) {
                    int v = userCatCount.get(user).getValueOrZero(c);
                    if (v > maxCate) {
                        maxCateName = c;
                    }
                    output.add(v);
                }
                output.add(maxCateName);
                output.add(buyCount.getValueOrZero(user));

                // buy or not buy
                output.add(orderMarkers.contains(user) ? "1" : "0");
                //                w.write(Joiner.on(",").join(output) + "\n");

                erOutput.add(user);
                erOutput.addAll(output);
                ew.write(Joiner.on(",").join(erOutput) + "\n");

            }

            // 輸入每行 1 個 user 
            // view count 
            // unique view count
            // CAT 1 => 筆數 
            // CAT 2 => 筆數
            // ...
            // CAT N => 筆數
            // 看最多的 CAT 名字
            // price 等級，看了多少價錢！？
        }

        protected List<String> getCategoryList() {
            Set<String> cates = new HashSet<String>();
            for (Set<String> set : userUniqueCatCount.values()) {
                cates.addAll(set);
            }
            List<String> ret = new ArrayList<String>(cates);
            Collections.sort(ret);
            return ret;
        }

    }

}
