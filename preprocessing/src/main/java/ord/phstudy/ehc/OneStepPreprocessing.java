package ord.phstudy.ehc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import file.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by study on 5/18/15.
 */
public class OneStepPreprocessing {
    final static int MIN_ACTION_COLON_POS = 56;

    // 290138
    final static Map<String, String> categories = Maps.newConcurrentMap();
    final static Map<String, Record> records = Maps.newConcurrentMap();

    final static Set<String> eruids = Sets.newHashSet();
    static boolean writeHeader = true;

    public static void main(String[] args) throws Exception {

        int corePoolSize = 3;
        int maximumPoolSize = 3;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        long startTime = System.currentTimeMillis();

        BufferedReader trainBr = FileManager.fileAsReader("EHC_2nd_round_train.log");
        BufferedReader testBr = FileManager.fileAsReader("EHC_2nd_round_test.log");
        BufferedWriter trainBw = FileManager.fileAsWriter("train.csv");
        BufferedWriter testBw = FileManager.fileAsWriter("test.csv");

        // Train Dataset
        String line;
        while ((line = trainBr.readLine()) != null) {
            pool.execute(new Task(line, true));
        }
        while (pool.getTaskCount() != pool.getCompletedTaskCount()) {
            System.err.println("count=" + pool.getTaskCount() + "," + pool.getCompletedTaskCount());
            Thread.sleep(1000);
        }
        writeTrainDataset(trainBw);

        long endTime = System.currentTimeMillis();
        System.out.println("Training dataset generation took " + (endTime - startTime) + " ms");


        // Test Dataset
        startTime = System.currentTimeMillis();
        while ((line = testBr.readLine()) != null) {
            pool.execute(new Task(line, false));
        }
        while (pool.getTaskCount() != pool.getCompletedTaskCount()) {
            System.err.println("count=" + pool.getTaskCount() + "," + pool.getCompletedTaskCount());
            Thread.sleep(1000);
        }
        writeTestDataset(testBw);
        pool.shutdown();

        endTime = System.currentTimeMillis();
        System.out.println("Testing dataset generation took " + (endTime - startTime) + " ms");


        // no op -> 6 secs
        // 1 thread no op -> 8 secs
        // 2 thread no op -> 10 secs
        // 3 thread no op -> 12 secs

        // "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"GET /action\\?;(.+?) HTTP/1.(\\d+)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
        // regex no op -> 54 secs
        // 1 thread regex no op -> 26 secs
        // 2 thread regex no op -> 25 secs
        // 3 thread regex no op -> 25 secs
        // 4 thread regex no op -> 30 secs
        // 8 thread regex no op -> 35 secs

        // line.substring(line.indexOf("[") + 1, line.indexOf("]"));
        // 1 thread no op -> 8040 ms
        // 3 thread no op -> 8040 ms

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
        if(writeHeader) {
            bw.write(Record.getHeader(true) + "\n");
        }
        Set<String> keys = records.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Record record = records.get(key);
            String pid = record.pid;
            if (record.cid.charAt(0) == ',' && categories.containsKey(pid)) {
                record.cid = categories.get(pid);
            }
            Integer p = PriceUtils.prices.get(pid);
            if (p != null) {
                record.price = p;
            }

            bw.write(record.toString() + "\n");
        }
        records.clear();
    }

    public static void writeTestDataset(BufferedWriter bw) throws IOException {
        if(writeHeader) {
            bw.write(Record.getHeader(false) + "\n");
        }
        Set<String> keys = records.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            Record record = records.get(key);
            String pid = record.pid;
            if (record.cid.charAt(0) == ',' && categories.containsKey(pid)) {
                record.cid = categories.get(pid);
            }
            Integer p = PriceUtils.prices.get(pid);
            if (p != null) {
                record.price = p;
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
        String key = pid + eruid;

        Record record;

        if (records.containsKey(key)) {
            record = records.get(key);
            record.hour = ExtractorUtils.extractHour(line);
            record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);
            record.viewnum++;
        } else {
            record = new Record();
            record.isTrain = isTrain;
            record.cid = ExtractorUtils.extractCategory(line);
            record.pid = pid;
            record.uid = ExtractorUtils.extractUid(line);
            //record.ip = ExtractorUtils.extractIp(line);
            //record.ua = extractUserAgent(line);
            record.eturec = ExtractorUtils.extractEturec(line);
            record.eruid = eruid;
            record.hour = ExtractorUtils.extractHour(line);
            record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);

            if (!record.cid.isEmpty()) {
                categories.put(record.pid, record.cid);
            }

            records.put(key, record);
        }
    }


    public static void processCart(String line, boolean isTrain) {
        String eruid = ExtractorUtils.extractEruid(line);
        String plist = ExtractorUtils.extractPlist(line);
        String[] products = plist.split(",");

        if (products.length > 1) { // has Product ?
            int mod = products.length % 3;
            int len = products.length - mod; // 2 records miss price

            for (int i = 0; i < len; i += 3) {
                String pid = products[i];
                String key = pid + eruid;
                short num = Short.parseShort(products[i + 1]);
                int price = Integer.parseInt(products[i + 2]);

                if (!PriceUtils.prices.containsKey(pid)) {
                    PriceUtils.prices.put(pid, price);
                }

                Record record;
                if (records.containsKey(key)) {
                    record = records.get(key);
                    record.num = num;
                    record.price = price;
                } else {
                    record = new Record();
                    record.isTrain = isTrain;
                    record.pid = pid;
                    record.num = num;
                    record.price = price;
                    record.uid = ExtractorUtils.extractUid(line);
                    //record.ip = ExtractorUtils.extractIp(line);
                    //record.ua = extractUserAgent(line);
                    record.eruid = eruid;
                    record.hour = ExtractorUtils.extractHour(line);
                    record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);
                }
            }
        }
    }


    public static void processOrder(String line, boolean isTrain) {
        String eruid = ExtractorUtils.extractEruid(line);
        String plist = ExtractorUtils.extractPlist(line);
        String[] products = plist.split(",");

        if (products.length > 1) { // has Product ?
            for (int i = 0; i < products.length; i += 3) {
                String pid = products[i];
                String key = pid + eruid;
                short num = Short.parseShort(products[i + 1]);
                int price = Integer.parseInt(products[i + 2]);

                if (!PriceUtils.prices.containsKey(pid)) {
                    PriceUtils.prices.put(pid, price);
                }

                Record record;
                if (records.containsKey(key)) {
                    record = records.get(key);
                    record.num += num; // same order sometimes appears multiple times...
                    record.price = price;
                    record.buy = 'Y';
                } else {
                    record = new Record();
                    record.isTrain = isTrain;
                    record.pid = pid;
                    record.num = num;
                    record.price = price;
                    record.uid = ExtractorUtils.extractUid(line);
                    //record.ip = ExtractorUtils.extractIp(line);
                    //record.ua = extractUserAgent(line);
                    record.buy = 'Y';
                    record.eruid = eruid;
                    record.hour = ExtractorUtils.extractHour(line);
                    record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);
                }
            }
        }
    }
}
