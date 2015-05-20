package ord.phstudy.ehc;

import file.FileManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by study on 5/20/15.
 */
public class FetchPrice {

    static BufferedReader categoryOnlyBr;
    static BufferedWriter categoryBw;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://iguang.tw/udn/product/";
    //static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c02.do?dc_cargxuid_0=";
    static int bashUrlLength = bashUrl.length();
    static Set<String> cids = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        int corePoolSize = 8;
        int maximumPoolSize = 20;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        //String categoryStr = "A_001_001_003_001";

        categoryOnlyBr = FileManager.fileAsReader("missing_products.csv");
        categoryBw = FileManager.fileAsWriter("products.csv");

        String line;
        while ((line = categoryOnlyBr.readLine()) != null) {
            if (cnt.getAndIncrement() % 10000 == 0) {
                categoryBw.flush();
            }
            pool.execute(new Task(line));
        }

        categoryBw.flush();
        categoryBw.close();
        pool.shutdown();
    }

    public static class Task implements Runnable {
        String line;

        public Task(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            try {
                String pid = "U" + line.substring(0, line.length() - 1);
                String url = bashUrl + pid + ".html";

                Document doc = Jsoup.connect(url).get();
                Elements anchors = doc.select("a[class=sublink]");
                Elements spans = doc.select("span[property=price]");


                String title = "";
                String price = "0";

                if (anchors.size() > 0) {
                    title = anchors.get(0).text().replace(",", "，");
                }
                if (spans.size() > 0) {
                    price = spans.get(0).text();
                }
                categoryBw.write(line + "," + pid + "," + title + "," + price + "\n");

            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    }
    // pdtname
    // itemprop="value"
}
