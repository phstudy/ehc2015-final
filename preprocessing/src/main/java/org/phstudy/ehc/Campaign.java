package org.phstudy.ehc;

import org.phstudy.ehc.utils.ExtractorUtils;
import org.qty.file.FileManager;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by study on 6/26/15.
 */
public class Campaign {
    public static void main(String[] args) throws Exception {
        Map<String, Set<String>> buy = new HashMap<>();
        Map<String, Set<String>> campaign = new HashMap<>();

        BufferedReader trainBr = FileManager.fileAsReader("EHC_2nd_round_train.log");

        trainBr.lines().forEach(line -> {
            char etu = ExtractorUtils.extractEturec(line);

            //if (etu == 'Y') {
            if (line.contains("utm_campaign")) {
                String pid = ExtractorUtils.extractPid(line);
                String eruid = ExtractorUtils.extractEruid(line);

                campaign.putIfAbsent(eruid, new HashSet<>());
                campaign.get(eruid).add(pid);
            }
            if (line.contains("=order")) {
                String plist = ExtractorUtils.extractPlist(line);
                String[] products = plist.split(",");

                String eruid = ExtractorUtils.extractEruid(line);
                for (int i = 0; i < products.length; i += 3) {
                    String pid = products[i];

                    buy.putIfAbsent(eruid, new HashSet<>());
                    buy.get(eruid).add(pid);
                }
            }
        });

        System.out.println(buy.size());
        System.out.println(campaign.size());

        int count[] = new int[]{0, 0};
        campaign.forEach((key, val) -> {
            count[1] += val.size();
            if (buy.get(key) != null) {
                buy.get(key).retainAll(val);
                if (buy.get(key).size() > 0) {
                    count[0] += buy.get(key).size();
                }
            }
        });
        System.out.println("utm_campaign" + count[1]);
        System.out.println(count[0]);
    }
}