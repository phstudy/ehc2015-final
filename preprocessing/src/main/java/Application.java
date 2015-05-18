import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import domain.ProductRecord;
import domain.Record;
import file.FileManager;

/**
 * Created by study on 5/14/15.
 */
public class Application {

    static SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
    static SimpleDateFormat targetSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);

    static File file(String name) {
        return FileManager.file(name);
    }

    public static void main(String[] args) throws Exception {

        int corePoolSize = 8;
        int maximumPoolSize = 20;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());


        final long startTime = System.currentTimeMillis();
        System.out.println("start: " + new Date());

        String regex = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"GET /action\\?;(.+?) HTTP/1.(\\d+)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";

        Pattern pattern = Pattern.compile(regex);

        // category
        BufferedWriter categoryBw = FileManager.fileAsWriter("train_category.csv"); 
        BufferedWriter categoryTestBw = FileManager.fileAsWriter("test_category.csv");
        Set<String> categoryPids = new HashSet<String>(200000);
        Set<String> categoryTestPids = new HashSet<String>(200000);
        //categoryBw.write("pid,class1,class2,class3,class4,class5\n");
        //categoryTestBw.write("pid,class1,class2,class3,class4,class5\n");

        // search
        BufferedWriter searchBw = FileManager.fileAsWriter("train_search.csv");
        BufferedWriter searchTestBw = FileManager.fileAsWriter("test_search.csv");
        //searchBw.write("ip,ts,uid,keywords,eruid\n");
        //searchTestBw.write("ip,ts,uid,keywords,eruid\n");

        // price
        BufferedWriter priceBw = FileManager.fileAsWriter("price.csv");
        Set<String> pricePids = new HashSet<String>(200000);
        //priceBw.write("pid,price\n");

        // order
        BufferedWriter orderBw = FileManager.fileAsWriter("order.csv");
        //orderBw.write("pid,ts,ip,price,num,uid,eruid\n");

        // view
        BufferedWriter viewBw = FileManager.fileAsWriter("train_view.csv");
        BufferedWriter viewTestBw = FileManager.fileAsWriter("test_view.csv");
        //viewBw.write("pid,ts,ip,uid,eruid\n");
        //viewTestBw.write("pid,ts,ip,uid,eruid\n");

        // datasets
        BufferedReader trainBr = FileManager.fileAsReader("EHC_2nd_round_train.log");
        BufferedReader testBr = FileManager.fileAsReader("EHC_2nd_round_test_clean.log");

        String line;
        while ((line = trainBr.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String ip = matcher.group(1);
                Date ts = null;
                try {
                    ts = sdf.parse(matcher.group(4));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Map<String, Object> data = requestToMap(matcher.group(5));
                int code = Integer.parseInt(matcher.group(7));
                int bytes = Integer.parseInt(matcher.group(8));
                String referer = matcher.group(9);

                //UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
                //ReadableUserAgent agent = parser.parse(matcher.group(10));
                Record rec = new Record(null, ip, ts, data, code, bytes, referer);

                // generate category dataset
                if ("view".equals(rec.getAct())) {
                    String pid = (String) rec.getData().get("pid");
                    if (!categoryPids.contains(pid)) {
                        categoryPids.add(pid);

                        List<String> categories = (List<String>) rec.getData().get("cat");
                        try {
                            StringBuilder sb = new StringBuilder(20);
                            sb.append(pid + ",");
                            for (int i = 0; i < 5; i++) {
                                if (i < categories.size()) {
                                    sb.append(categories.get(i));
                                }
                                if (i == 4) {
                                    sb.append("\n");
                                } else {
                                    sb.append(",");
                                }
                            }
                            categoryBw.write(sb.toString());
                        } catch (Exception e) {
                            System.out.println(rec);
                            e.printStackTrace();
                        }
                    }
                } else if ("cart".equals(rec.getAct()) || "order".equals(rec.getAct())) {
                    List<ProductRecord> plist = (List<ProductRecord>) rec.getData().get("plist");

                    for (ProductRecord productRecord : plist) {
                        if (!pricePids.contains(productRecord.getPid())) {
                            pricePids.add(productRecord.getPid());

                            try {
                                priceBw.write(productRecord.getPid() + "," + productRecord.getPrice() + "\n");
                            } catch (Exception e) {
                                System.out.println(rec);
                                e.printStackTrace();
                            }
                        }
                    }
                    ;
                } else if ("search".equals(rec.getAct())) {
                    try {
                        StringBuilder sb = new StringBuilder(20);
                        sb.append(rec.getIp() + ",");
                        sb.append(targetSdf.format(rec.getTs()) + ",");
                        sb.append(rec.getData().get("uid") + ",");
                        sb.append(rec.getData().get("keywords") + ",");
                        sb.append(rec.getData().get("erUid"));

                        searchBw.write(sb.toString() + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if ("view".equals(rec.getAct())) {
                    extractViewRecord(viewBw, rec);
                }
                if ("order".equals(rec.getAct())) {
                    extractOrderRecord(orderBw, rec);
                }
            }
        }

        while ((line = testBr.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String ip = matcher.group(1);
                Date ts = null;
                try {
                    ts = sdf.parse(matcher.group(4));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Map<String, Object> data = requestToMap(matcher.group(5));
                int code = Integer.parseInt(matcher.group(7));
                int bytes = Integer.parseInt(matcher.group(8));
                String referer = matcher.group(9);

                //UserAgent agent = UserAgent.parseUserAgentString(matcher.group(10));
                Record rec = new Record(null, ip, ts, data, code, bytes, referer);

                // generate category dataset
                if ("view".equals(rec.getAct())) {
                    String pid = (String) rec.getData().get("pid");
                    if (!categoryTestPids.contains(pid)) {
                        categoryTestPids.add(pid);

                        List<String> categories = (List<String>) rec.getData().get("cat");
                        try {
                            StringBuilder sb = new StringBuilder(20);
                            sb.append(pid + ",");
                            for (int i = 0; i < 5; i++) {
                                if (i < categories.size()) {
                                    sb.append(categories.get(i));
                                }
                                if (i == 4) {
                                    sb.append("\n");
                                } else {
                                    sb.append(",");
                                }
                            }
                            categoryTestBw.write(sb.toString());
                        } catch (Exception e) {
                            System.out.println(rec);
                            e.printStackTrace();
                        }
                    }
                } else if ("search".equals(rec.getAct())) {
                    try {
                        StringBuilder sb = new StringBuilder(20);
                        sb.append(rec.getIp() + ",");
                        sb.append(targetSdf.format(rec.getTs()) + ",");
                        sb.append(rec.getData().get("uid") + ",");
                        sb.append(rec.getData().get("keywords") + ",");
                        sb.append(rec.getData().get("erUid"));

                        searchTestBw.write(sb.toString() + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if ("view".equals(rec.getAct())) {
                    extractViewRecord(viewTestBw, rec);
                }
            }
        }

        final long endTime = System.currentTimeMillis();
        System.out.println("done: " + new Date());
        System.out.println("Total execution time: " + (endTime - startTime));

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Category.containCategory, Category.toCategory, Category.toCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Category.containCategory, Category.toCategory, Category.toCount));

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Plist.containPlist, Plist.toPlist, Plist.toUniqueCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Plist.containPlist, Plist.toPlist, Plist.toUniqueCount));

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Plist.containPlist, Plist.toPlist, Plist.toCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Plist.containPlist, Plist.toPlist, Plist.toCount));
    }

    protected static void extractOrderRecord(BufferedWriter orderBw, Record rec) {
        Map<String, Object> orderData = rec.getData();
        List<ProductRecord> plist = (List<ProductRecord>) orderData.get("plist");
        // Record{ua=null, ip='220.129.169.163', ts=Sun Feb 01 00:45:43 CST 2015, 
        // data={uid=U180944017, plist=[{pid="0014560652", price=849, num=1}], act=order, 
        // erUid=6c685cbe-cc40-609a-3da2-d48e4ed7b771}, code=302, bytes=160, referer='-'}

        Object uid = orderData.get("uid");
        Object erUid = orderData.get("erUid");

        StringBuilder sb = new StringBuilder();

        for (ProductRecord productRecord : plist) {
            sb.setLength(0);
            // pid,ts,ip,price,num,uid,eruid
            sb.append(productRecord.getPid()).append(",");
            sb.append(targetSdf.format(rec.getTs())).append(",");
            sb.append(rec.getIp()).append(",");
            sb.append(productRecord.getPrice()).append(",");
            sb.append(productRecord.getNum()).append(",");
            sb.append(uid).append(",");
            sb.append(erUid).append("\n");
            try {
                orderBw.write(sb.toString());
            } catch (Exception e) {
            }
        }
    }

    protected static void extractViewRecord(BufferedWriter viewBw, Record rec) {
        // Record{ua=null, ip='218.164.82.134', ts=Sun Feb 01 00:56:16 CST 2015, 
        // data={uid=, act=view, cat=[H, H_002, H_002_013, H_002_013_007, H_002_013_007_006], 
        // erUid=4547674a-2322-9350-c6d2-7fbbe1133e5e, pid=0012586604}, 
        // code=302, bytes=160, referer='-'}


        // pid,ts,ip,uid,eruid
        Map<String, Object> orderData = rec.getData();
        Object pid = orderData.get("pid");
        Object uid = orderData.get("uid");
        Object erUid = orderData.get("erUid");

        StringBuilder viewSb = new StringBuilder();
        viewSb.append(pid).append(",");
        viewSb.append(targetSdf.format(rec.getTs())).append(",");
        viewSb.append(rec.getIp()).append(",");
        viewSb.append(uid).append(",");
        viewSb.append(erUid).append(",");
        String eturec = orderData.get("eturec") == null ? "0" : "1";
        viewSb.append(eturec).append("\n");

        try {
            viewBw.write(viewSb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> requestToMap(String str) {
        Map<String, Object> rst = new HashMap<String, Object>(10);
        int lastChar = str.charAt(str.length() - 1);
        if (lastChar == ';') {
            str = str.substring(0, str.length() - 1);
        }
        String[] parts = str.split(";");

        for (String part : parts) {
            String[] elements = part.split("=");
            String key = elements[0];
            String value = "";
            if (elements.length == 2) {
                value = elements[1];
            }
            Object object = value;

            if ("keywords".equals(key)) {
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                    value = URLDecoder.decode(value, "UTF-8");
                    value = value.replace(',', '|');
                    object = value;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if ("cat".equals(key)) {
                object = categoryToList(value);
            }

            if ("plist".equals(key)) {
                object = plistToList(value);
            }

            rst.put(key, object);
        }

        return rst;
    }

    public static List<String> categoryToList(String categories) {
        String[] parts = categories.split(",");
        List<String> rst;
        if (parts.length > 0 && parts[0].length() != 1) {
            rst = new ArrayList<String>(5);
            String[] cparts = parts[0].split("_");
            String str = cparts[0];
            rst.add(str);
            for (int i = 1; i < cparts.length; i++) {
                str += "_" + cparts[i];
                rst.add(str);
            }
        } else {
            rst = Arrays.asList(parts);
        }

        return rst;
    }

    public static List<ProductRecord> plistToList(String plist) {
        List<ProductRecord> rst = new ArrayList<ProductRecord>();

        String[] records = plist.split(",");

        if (records.length > 1) {
            for (int i = 0; i < records.length; i += 3) {
                try {
                    String pid = records[i];
                    int num = Integer.parseInt(records[i + 1]);
                    int price = Integer.parseInt(records[i + 2]);

                    rst.add(new ProductRecord(pid, num, price));
                } catch (Exception e) {
                    System.out.println(plist);
                    e.printStackTrace();
                }
            }
        }

        return rst;
    }

//    public static IntSummaryStatistics getSummaryStatistics(String filename, Predicate<String> filter,
//                                                            Function<String, String> map, ToIntFunction<String> toInt) throws Exception {
//        File file = new File(filename);
//        BufferedReader br = new BufferedReader(new FileReader(file));
//        return br.lines().filter(filter).map(map).mapToInt(toInt).filter(val -> val > 0).summaryStatistics();
//    }
}
