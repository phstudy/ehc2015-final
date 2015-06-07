package org.qty.stat;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.qty.ItemCounter;
import org.qty.QLabInitConfig;
import org.qty.file.FileManager;
import org.qty.validate.ValidationUtils;

public class TrainingStat {

    public static void main(String[] args) throws Exception {

        int orderCount = 0;

        // 每個 order 買幾個的不同的 pid 統計
        ItemCounter<Integer> pidPerSession = new ItemCounter<Integer>();

        // 每個 order 買幾個的統計
        ItemCounter<Integer> countPerSession = new ItemCounter<Integer>();

        // 消費金額統計
        ItemCounter<Integer> saleAmount = new ItemCounter<Integer>();

        for (String s : FileManager.fileAsLineIterator(QLabInitConfig.TRAIN_FILE)) {

            //            String eruid = ValidationUtils.eruid(s);
            //            String pid = ValidationUtils.pid(s);

            if (!s.contains("act=o")) {
                continue;
            }

            String plist = ValidationUtils.plist(s);
            if (StringUtils.isEmpty(plist)) {
                continue;
            }

            /////////////
            String[] pArray = plist.split(",");
            if (pArray.length % 3 != 0) {
                continue;
            }

            orderCount++;

            int howManyPidInAnOrder = pArray.length / 3;
            pidPerSession.count(howManyPidInAnOrder);

            int money = 0;
            int buyCount = 0;
            for (int i = 0; i < pArray.length; i += 3) {
                int bc = NumberUtils.toInt(pArray[i + 1]);
                int c = NumberUtils.toInt(pArray[i + 2]);
                buyCount += bc;
                money += (bc * c);

            }
            countPerSession.count(buyCount);
            saleAmount.count(1 + (money / 1000));
        }

        System.out.println("order count: " + orderCount);
        System.out.println(pidPerSession);

        ArrayList<Integer> jjj = new ArrayList<Integer>();
        for (int i = 1; i <= Collections.max(pidPerSession.key()); i++) {
            jjj.add(pidPerSession.getValueOrZero(i));
        }
        System.out.println(jjj);

        ArrayList<Integer> xxx = new ArrayList<Integer>();
        for (int i = 1; i <= Collections.max(countPerSession.key()); i++) {
            xxx.add(countPerSession.getValueOrZero(i));
        }
        System.out.println(xxx);

        System.out.println(countPerSession);

        ArrayList<Integer> mmm = new ArrayList<Integer>();
        for (int i = 1; i <= 50; i++) {
            mmm.add(saleAmount.getValueOrZero(i));
        }
        System.out.println(mmm);
        System.out.println(saleAmount);

    }
}
