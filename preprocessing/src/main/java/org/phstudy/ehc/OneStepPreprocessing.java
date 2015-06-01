package org.phstudy.ehc;

import com.google.common.collect.Maps;
import org.phstudy.ehc.domain.Record;
import org.phstudy.ehc.utils.ExtractorUtils;
import org.phstudy.ehc.utils.GuessUtils;
import org.phstudy.ehc.utils.PriceUtils;
import org.qty.file.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by study on 5/18/15.
 */
public class OneStepPreprocessing {
    final static int MIN_ACTION_COLON_POS = 56;

    final static Map<String, String> categories = Maps.newConcurrentMap();
    final static Map<String, Record> records = Maps.newConcurrentMap();

    static boolean writeHeader = false;
    static boolean applyGuess = true;


    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        BufferedReader trainBr = FileManager.fileAsReader("EHC_2nd_round_train.log");
        BufferedReader testBr = FileManager.fileAsReader("EHC_2nd_round_test.log");
        BufferedWriter trainBw = FileManager.fileAsWriter("train.csv");
        BufferedWriter testBw = FileManager.fileAsWriter("test.csv");

        // Train Dataset
        String line;
        while ((line = trainBr.readLine()) != null) {
            new Task(line, true).run();
        }

        writeTrainDataset(trainBw);

        long endTime = System.currentTimeMillis();
        System.out.println("Training dataset generation took " + (endTime - startTime) + " ms");

        // Test Dataset
        startTime = System.currentTimeMillis();
        while ((line = testBr.readLine()) != null) {
            new Task(line, false).run();
        }

        writeTestDataset(testBw);

        endTime = System.currentTimeMillis();
        System.out.println("Testing dataset generation took " + (endTime - startTime) + " ms");
    }

    public static class Task implements Runnable {
        boolean isTrain = false;
        String line;

        public Task(String line, boolean isTrain) {
            this.line = line;
            this.isTrain = isTrain;
        }

        @Override
        public void run() {
            int actIdx = line.indexOf("act=", MIN_ACTION_COLON_POS);
            int type = line.charAt(actIdx + 4);

            if (type == 'v') { // view 86%
                processView(line, isTrain);
            } else if (type == 's') { // search 6%
                return;
            } else if (type == 'c') { // cart 7%
                processCart(line, isTrain);
            } else if (type == 'o') { // order 1%
                processOrder(line, isTrain);
            }
        }
    }

    public static void writeTrainDataset(BufferedWriter bw) throws IOException {
        if (writeHeader) {
            bw.write(Record.getHeader(true) + "\n");
        }
        Set<String> keys = records.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Record record = records.get(key);
            String pid = record.pid;

            if (applyGuess && GuessUtils.guess.contains(pid)) {
                continue;
            }

            String upid = Record.pidToUpid(record.pid);
            if (record.cid.charAt(0) == ',' && categories.containsKey(upid)) {
                record.cid = categories.get(upid);
            }

            if (record.price == 0) {
                Integer p = PriceUtils.prices.get(pid);
                if (p != null) {
                    record.price = p;
                }
            }

            bw.write(record.toString() + "\n");
        }
        records.clear();
    }

    public static void writeTestDataset(BufferedWriter bw) throws IOException {
        if (writeHeader) {
            bw.write(Record.getHeader(false) + "\n");
        }
        Set<String> keys = records.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Record record = records.get(key);
            String pid = record.pid;

            if (applyGuess && GuessUtils.guess.contains(pid)) {
                continue;
            }

            String upid = Record.pidToUpid(record.pid);
            if (record.cid.charAt(0) == ',' && categories.containsKey(upid)) {
                record.cid = categories.get(upid);
            }
            if (record.price == 0) {
                Integer p = PriceUtils.prices.get(pid);
                if (p != null) {
                    record.price = p;
                }
            }

            bw.write(record.toString() + "\n");
        }
    }

    public static void processView(String line, boolean isTrain) {
        String pid = ExtractorUtils.extractPid(line);
        if (pid.isEmpty()) {
            return;
        }
        String eruid = ExtractorUtils.extractEruid(line);
        if (eruid == null) {
            return;
        }

        String upid = Record.pidToUpid(pid);
        String key = upid + eruid;

        Record record;

        if (records.containsKey(key)) {
            record = records.get(key);
            record.day = ExtractorUtils.extractDay(line);
            record.hour = ExtractorUtils.extractHour(line);
            record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);
            record.viewnum++;
        } else {
            record = new Record();
            record.isTrain = isTrain;
            record.cid = ExtractorUtils.extractCategory(upid, line);
            record.pid = pid;
            record.uid = ExtractorUtils.extractUid(line);
            //record.ip = ExtractorUtils.extractIp(line);
            record.device = ExtractorUtils.extractDevice(line);
            record.eturec = ExtractorUtils.extractEturec(line);
            record.eruid = eruid;
            record.day = ExtractorUtils.extractDay(line);
            record.hour = ExtractorUtils.extractHour(line);
            record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);

            if (!record.cid.isEmpty()) {
                categories.put(upid, record.cid);
            }

            records.put(key, record);
        }
    }


    public static void processCart(String line, boolean isTrain) {
        String eruid = ExtractorUtils.extractEruid(line);
        if (eruid == null) {
            return;
        }
        String plist = ExtractorUtils.extractPlist(line);
        String[] products = plist.split(",");

        if (products.length > 1) { // has Product ?
            int mod = products.length % 3;
            int len = products.length - mod; // 2 records miss price

            for (int i = 0; i < len; i += 3) {
                String pid = products[i];

                String upid = Record.pidToUpid(pid);
                String key = upid + eruid;
                int price = Integer.parseInt(products[i + 2]);

                PriceUtils.prices.put(pid, price); // update price

                Record record;
                if (records.containsKey(key)) {
                    record = records.get(key);
                    record.pid = pid;
                    record.price = price;
                } else {
                    record = new Record();
                    record.isTrain = isTrain;
                    if (Record.DEFAULT_CID.equals(record.cid)) {
                        record.cid = ExtractorUtils.extractPredefinedCategory(upid);
                    }
                    record.pid = pid;
                    record.price = price;
                    record.uid = ExtractorUtils.extractUid(line);
                    //record.ip = ExtractorUtils.extractIp(line);
                    record.device = ExtractorUtils.extractDevice(line);
                    record.eruid = eruid;
                    record.day = ExtractorUtils.extractDay(line);
                    record.hour = ExtractorUtils.extractHour(line);
                    record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);

                    records.put(key, record);
                }
            }
        }
    }


    public static void processOrder(String line, boolean isTrain) {
        String eruid = ExtractorUtils.extractEruid(line);
        if (eruid == null) {
            return;
        }
        String plist = ExtractorUtils.extractPlist(line);
        String[] products = plist.split(",");


        if (products.length > 1) { // has Product ?
            for (int i = 0; i < products.length; i += 3) {
                String pid = products[i];

                String upid = Record.pidToUpid(pid);
                String key = upid + eruid;
                short num = Short.parseShort(products[i + 1]);
                int price = Integer.parseInt(products[i + 2]);

                PriceUtils.prices.put(pid, price); // update price

                Record record;
                if (records.containsKey(key)) {
                    record = records.get(key);
                    record.pid = pid;
                    record.num += num; // same order sometimes appears multiple times...
                    record.price = price;
                    record.buy = 'Y';
                } else {
                    record = new Record();
                    record.isTrain = isTrain;
                    if (Record.DEFAULT_CID.equals(record.cid)) {
                        record.cid = ExtractorUtils.extractPredefinedCategory(upid);
                    }
                    record.pid = pid;
                    record.num = num;
                    record.price = price;
                    //record.uid = ExtractorUtils.extractUid(line); // skip uid
                    //record.ip = ExtractorUtils.extractIp(line);
                    record.device = ExtractorUtils.extractDevice(line);
                    record.buy = 'Y';
                    record.eruid = eruid;
                    record.day = ExtractorUtils.extractDay(line);
                    record.hour = ExtractorUtils.extractHour(line);
                    record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);

                    records.put(key, record);
                }
            }
        }
    }
}
