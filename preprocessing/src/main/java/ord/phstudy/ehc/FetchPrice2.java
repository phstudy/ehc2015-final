package ord.phstudy.ehc;

import file.FileManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by study on 5/20/15.
 */
public class FetchPrice2 {

    static BufferedReader categoryOnlyBr;
    static BufferedWriter categoryBw, categoryBw2, categoryBw3;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://www.cheapcheap.com.tw/kw?q=";

    public static void main(String[] args) throws Exception {
        int corePoolSize = 3;
        int maximumPoolSize = 20;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        //String categoryStr = "A_001_001_003_001";

        categoryOnlyBr = FileManager.fileAsReader("products_full.csv");
        categoryBw = FileManager.fileAsWriter("products2_full.csv");
        categoryBw2 = FileManager.fileAsWriter("products2_price_nonzero.csv");
        categoryBw3 = FileManager.fileAsWriter("products2_price_zero.csv");


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
                String[] parts = line.split(",");
                String pid = parts[0];
                String upid = parts[1];
                String price = parts[2];
                String title = parts[3];

                if (!"0".equals(price)) { // skip
                    return;
                }

                String url = bashUrl + title;

                Document doc = Jsoup.connect(url).get();

                Elements anchors = doc.select("div[class=product-price]");

                if (anchors.size() > 0) {
                    Elements spans = anchors.select("span");
                    if (spans.size() > 0) {
                        price = spans.get(0).text();
                    }
                }
                categoryBw.write(pid + "," + upid + "," + title + "," + price + "\n");

                if ("0".equals(price)) {
                    categoryBw3.write(pid + "," + price + "\n");
                } else {
                    categoryBw2.write(pid + "," + price + "\n");
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

        }
    }
}
