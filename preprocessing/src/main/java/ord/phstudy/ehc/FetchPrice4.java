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
public class FetchPrice4 {
    static BufferedReader sourceBr;
    static BufferedWriter targetBw;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c02.do?dc_cargxuid_0=";

    public static void main(String[] args) throws Exception {
        int corePoolSize = 2;
        int maximumPoolSize = 2;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        sourceBr = FileManager.fileAsReader("products_full.csv");
        targetBw = FileManager.fileAsWriter("products4_full.csv");

        String line;

        while ((line = sourceBr.readLine()) != null) {
            pool.execute(new Task(line));
        }

        pool.awaitTermination(60, TimeUnit.MINUTES);

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
            String upid = parts[2];
            String title = "";
            String price = parts[1];

            if (parts.length > 3) {
                title = parts[3];
            }

            try {
                if (!"0".equals(price)) { // skip
                    return;
                }
                String url = bashUrl + upid;
                Document doc = Jsoup.connect(url)
                        .get();

                Elements titles = doc.select("span[itemprop=name]");

                if (titles.size() > 0) {
                    title = titles.get(0).text().replace(",", "，");

                    Elements values = doc.select("span[itemprop=value]");
                    if (values.size() > 0) {
                        price = values.get(0).text().replace(",", "，").trim();

                        targetBw.write(pid + "," + price + "," + upid + "," + title + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
