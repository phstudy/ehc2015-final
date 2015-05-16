import domain.ProductRecord;
import domain.Record;

import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by study on 5/14/15.
 */
public class Application {

    static String baseDir = "/Users/study/Desktop/EHC";

    static {

        String preferedBaseDir = System.getProperty("EHC_FINAL_DATASET_DIR");
        if (preferedBaseDir != null && new File(preferedBaseDir).exists() && new File(preferedBaseDir).isDirectory()) {
            baseDir = new File(preferedBaseDir).getAbsolutePath();
            System.err.println("reset base-dir as " + baseDir);
        }
    }

    static File file(String name) {
        return new File(baseDir, name);
    }

    public static void main(String[] args) throws Exception {

        String regex = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"GET /action\\?;(.+?) HTTP/1.(\\d+)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";

        Pattern pattern = Pattern.compile(regex);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

        // category
        BufferedWriter categoryBw = new BufferedWriter(new FileWriter(file("train_category.csv")));
        BufferedWriter categoryTestBw = new BufferedWriter(new FileWriter(file("test_category.csv")));
        Set<String> categoryPids = new HashSet<>(200000);
        Set<String> categoryTestPids = new HashSet<>(200000);
        categoryBw.write("pid,class1,class2,class3,class4,class5\n");
        categoryTestBw.write("pid,class1,class2,class3,class4,class5\n");

        // search
        BufferedWriter searchBw = new BufferedWriter(new FileWriter(file("train_search.csv")));
        BufferedWriter searchTestBw = new BufferedWriter(new FileWriter(file("test_search.csv")));
        searchBw.write("ip,ts,uid,keywords,eruid\n");
        searchTestBw.write("ip,ts,uid,keywords,eruid\n");

        // price
        BufferedWriter pricebw = new BufferedWriter(new FileWriter(file("price.csv")));
        Set<String> pricePids = new HashSet<>(200000);

        // order
        BufferedWriter orderBw = new BufferedWriter(new FileWriter(file("order.csv")));
        orderBw.write("pid,ts,ip,price,num,uid,eruid\n");

        // datasets
        BufferedReader trainBr = new BufferedReader(new FileReader(file("EHC_2nd_round_train.log")));
        BufferedReader testBr = new BufferedReader(new FileReader(file("EHC_2nd_round_test_clean.log")));

        trainBr.lines().forEach(line -> {
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

                    plist.forEach(productRecord -> {
                        if (!pricePids.contains(productRecord.getPid())) {
                            pricePids.add(productRecord.getPid());

                            try {
                                pricebw.write(productRecord.getPid() + "," + productRecord.getPrice() + "\n");
                            } catch (Exception e) {
                                System.out.println(rec);
                                e.printStackTrace();
                            }
                        }
                    });
                } else if ("search".equals(rec.getAct())) {
                    try {
                        StringBuilder sb = new StringBuilder(20);
                        sb.append(rec.getIp() + ",");
                        sb.append(rec.getTs().getTime() + ",");
                        sb.append(rec.getData().get("uid") + ",");
                        sb.append(rec.getData().get("keywords") + ",");
                        sb.append(rec.getData().get("erUid"));

                        searchBw.write(sb.toString() + "\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if ("order".equals(rec.getAct())){
                    Map<String, Object> orderData = rec.getData();
                    List<ProductRecord> plist = (List<ProductRecord>) orderData.get("plist");
                    // Record{ua=null, ip='220.129.169.163', ts=Sun Feb 01 00:45:43 CST 2015, 
                    // data={uid=U180944017, plist=[{pid="0014560652", price=849, num=1}], act=order, 
                    // erUid=6c685cbe-cc40-609a-3da2-d48e4ed7b771}, code=302, bytes=160, referer='-'}

                    Object uid = orderData.get("uid");
                    Object erUid = orderData.get("erUid");
                    
                    StringBuilder sb = new StringBuilder();
                    plist.forEach(productRecord -> {
                        sb.setLength(0);
                        // pid,ts,ip,price,num,uid,eruid
                        sb.append(productRecord.getPid()).append(",");
                        sb.append(rec.getTs().getTime()).append(",");
                        sb.append(rec.getIp()).append(",");
                        sb.append(productRecord.getPrice()).append(",");
                        sb.append(productRecord.getNum()).append(",");
                        sb.append(uid).append(",");
                        sb.append(erUid).append("\n");
                        try {
                            orderBw.write(sb.toString());
                        } catch (Exception e) {
                        }
                    });
                }
            }
        });

        testBr.lines().forEach(line -> {
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
                        sb.append(rec.getTs().getTime() + ",");
                        sb.append(rec.getData().get("uid") + ",");
                        sb.append(rec.getData().get("keywords") + ",");
                        sb.append(rec.getData().get("erUid"));

                        searchTestBw.write(sb.toString() + "\n");                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        categoryBw.flush();
        categoryBw.close();

        categoryTestBw.flush();
        categoryTestBw.close();

        searchTestBw.flush();
        searchTestBw.close();

        pricebw.flush();
        pricebw.close();

        trainBr.close();
        testBr.close();
        
        orderBw.close();

        //UADetectorServiceFactory.getOnlineUpdatingParser().shutdown();

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Category.containCategory, Category.toCategory, Category.toCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Category.containCategory, Category.toCategory, Category.toCount));

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Plist.containPlist, Plist.toPlist, Plist.toUniqueCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Plist.containPlist, Plist.toPlist, Plist.toUniqueCount));

        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_train.log", Plist.containPlist, Plist.toPlist, Plist.toCount));
        //System.out.println(getSummaryStatistics("/Users/study/Desktop/EHC/EHC_2nd_round_test_clean.log", Plist.containPlist, Plist.toPlist, Plist.toCount));
    }

    public static Map<String, Object> requestToMap(String str) {
        Map<String, Object> rst = new HashMap<>(10);
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
                    value.replace(',', '|');
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
            rst = new ArrayList<>(5);
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
        List<ProductRecord> rst = new ArrayList<>();

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

    public static IntSummaryStatistics getSummaryStatistics(String filename, Predicate<String> filter,
                                                            Function<String, String> map, ToIntFunction<String> toInt) throws Exception {
        File file = new File(filename);
        BufferedReader br = new BufferedReader(new FileReader(file));
        return br.lines().filter(filter).map(map).mapToInt(toInt).filter(val -> val > 0).summaryStatistics();
    }
}
