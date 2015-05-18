package ord.phstudy.ehc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import FileManager;

import java.io.BufferedReader;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by study on 5/18/15.
 */
public class Test {
    final static int MIN_ACTION_COLON_POS = 56;

    // 290138
    final static Map<String, String> categories = Maps.newConcurrentMap();
    final static Map<String, Record> records = Maps.newConcurrentMap();
    final static Set<String> uids = Sets.newConcurrentHashSet();

    public static void main(String[] args) throws Exception {
        int corePoolSize = 3;
        int maximumPoolSize = 3;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        long startTime = System.currentTimeMillis();

        BufferedReader trainBr = FileManager.fileAsReader("EHC_2nd_round_train.log");
        BufferedReader testBr = FileManager.fileAsReader("EHC_2nd_round_test.log");

//        int min = Integer.MAX_VALUE;

        String line;
        while ((line = trainBr.readLine()) != null) {
            pool.execute(new Task(line));

//            int idx = line.indexOf("eturec=");
//            if (min > idx && idx != -1) {
//                min = idx;
//            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("done: " + new Date());
        System.out.println("Total execution time: " + (endTime - startTime));

        while ((line = testBr.readLine()) != null) {
            pool.execute(new Task(line));

//            int idx = line.indexOf("eturec=");
//            if (min > idx && idx != -1) {
//                min = idx;
//            }
        }

        //1588855
//        System.out.println(min);

        pool.shutdown();

        endTime = System.currentTimeMillis();
        System.out.println("done: " + new Date());
        System.out.println("Total execution time: " + (endTime - startTime));


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
        String line;

        public Task(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            int actIdx = line.indexOf("act=", MIN_ACTION_COLON_POS);
            int type = line.charAt(actIdx + 4);

            if (type == 'v') { // view
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
                    record.viewnum = 1;
                    record.cid = ExtractorUtils.extractCategory(line);
                    record.pid = pid;
                    record.uid = ExtractorUtils.extractUid(line);
                    record.ip = ExtractorUtils.extractIp(line);
                    //record.ua = extractUserAgent(line);
                    record.etured = ExtractorUtils.extractEturec(line);
                    record.eruid = eruid;
                    record.hour = ExtractorUtils.extractHour(line);
                    record.weekOfDay = ExtractorUtils.extractWeekOfDay(line);

                    if (!record.cid.isEmpty()) {
                        categories.put(record.pid, record.cid);
                    }
                    if (record.uid.isEmpty()) {
                        uids.add(record.uid);
                    }

                    records.put(key, record);
                }
            } else if (type == 's') { // search
                return;
            } else if (type == 'c') { // order
                String plist = ExtractorUtils.extractPlist(line);
                //System.out.println(plist);
            } else if (type == 'o') { // order
                String plist = ExtractorUtils.extractPlist(line);
                //System.out.println(plist);
            }
        }
    }


}
