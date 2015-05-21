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
public class FetchPrice3 {

    static BufferedReader categoryOnlyBr;
    static BufferedWriter categoryBw, categoryBw2, categoryBw3, categoryBw4;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://pk.emailcash.com.tw/catalog.asp";

    public static void main(String[] args) throws Exception {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        //String categoryStr = "A_001_001_003_001";

        categoryOnlyBr = FileManager.fileAsReader("products2_full.csv");
        categoryBw = FileManager.fileAsWriter("products3_full.csv");
        categoryBw2 = FileManager.fileAsWriter("products3_price_nonzero.csv");
        categoryBw3 = FileManager.fileAsWriter("products3_price_zero.csv");
        categoryBw4 = FileManager.fileAsWriter("products3_fail.csv");


        String line;
        while ((line = categoryOnlyBr.readLine()) != null) {
            if (cnt.getAndIncrement() % 100 == 0) {
                categoryBw.flush();
                categoryBw2.flush();
                categoryBw3.flush();
                categoryBw4.flush();
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
            String[] parts = line.split(",");
            String pid = parts[0];
            String upid = parts[1];
            String title = parts[2];
            String price = parts[3];

            try {
                if (!"0".equals(price)) { // skip
                    return;
                }
                String url = bashUrl;
                Document doc = Jsoup.connect(url).timeout(60000)
                        .cookie("ASPSESSIONIDQSDQRBCD", "NAIHKAMBJOLBKIFOJLPIHGCO")
                        .data("keyword", title)
                        .data("searchCatalog", "0")
                        .post();

                Elements spans = doc.select("span[class=style18]");

                if (spans.size() > 0) {
                    price = spans.get(0).text().replace(",", "").replace("$", "");
                }
                categoryBw.write(pid + "," + upid + "," + title + "," + price + "\n");

                if ("0".equals(price)) {
                    categoryBw3.write(pid + "," + price + "\n");
                } else {
                    categoryBw2.write(pid + "," + price + "\n");
                }
            } catch (Exception e) {
                try {
                    categoryBw4.write(pid + "," + upid + "," + title + "," + price + "\n");
                } catch(Exception e1) {
                    e1.printStackTrace();
                }

                e.printStackTrace();
            }

        }
    }
}
