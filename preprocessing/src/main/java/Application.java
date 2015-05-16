import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import domain.ProductRecord;
import domain.Record;

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

        File trainFile = file("EHC_2nd_round_train.log");
        File testFile = file("EHC_2nd_round_test_clean.log");
        File categoryFile = file("category.csv");
        File priceFile = file("price.csv");

        BufferedWriter cbw = new BufferedWriter(new FileWriter(categoryFile));
        Set<String> cbw_pids = new HashSet<>(200000);

        BufferedWriter pbw = new BufferedWriter(new FileWriter(priceFile));
        Set<String> pbw_pids = new HashSet<>(200000);

        cbw.write("pid,1_class,2_class,3_class,4_class,5_class\n");

        BufferedReader trainBr = new BufferedReader(new FileReader(trainFile));

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
                    if (!cbw_pids.contains(pid) || true) {
                        cbw_pids.add(pid);

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
                            cbw.write(sb.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if ("cart".equals(rec.getAct()) || "order".equals(rec.getAct())) {
                    List<ProductRecord> plist = (List<ProductRecord>) rec.getData().get("plist");

                    plist.forEach(productRecord -> {
                        if (!pbw_pids.contains(productRecord.getPid())) {
                            pbw_pids.add(productRecord.getPid());

                            try {
                                pbw.write(productRecord.getPid() + "," + productRecord.getPrice() + "\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        BufferedReader testBr = new BufferedReader(new FileReader(testFile));

        cbw.flush();
        cbw.close();

        pbw.flush();
        pbw.close();

        trainBr.close();
        testBr.close();

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
