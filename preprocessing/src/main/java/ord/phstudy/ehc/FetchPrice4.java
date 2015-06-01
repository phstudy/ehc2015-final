package ord.phstudy.ehc;

import com.google.common.collect.Sets;
import file.FileManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Set;
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
    static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c10.do?dc_btn_0=Func_FormalPreview&dc_cargxuid_0=";

    static Set<String> pids = Sets.newConcurrentHashSet();

    public static void main(String[] args) throws Exception {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        sourceBr = FileManager.fileAsReader("product.csv");
        targetBw = FileManager.fileAsWriter("product_full.csv");

        String line;

        while ((line = sourceBr.readLine()) != null) {
            if (cnt.intValue() % 1000 == 0) {
                targetBw.flush();
                System.out.println(cnt.incrementAndGet());
            }
            pool.execute(new Task(line));
            cnt.incrementAndGet();
        }
        targetBw.flush();

        pool.awaitTermination(1, TimeUnit.MINUTES);

        pool.shutdown();
    }

    public static class Task implements Runnable {
        String line;

        public Task(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            //String[] parts = line.split(","); // 0005949565,0,U000594956,
            String upid = line;
            //String upid = parts[2];
            //String title = "";
            //String price = parts[1];

//            if (parts.length > 3) {
//                title = parts[3];
//            }

            if (pids.contains(upid)) {
                return;
            } else {
                pids.add(upid);
            }

            try {
                String url = bashUrl + "U" + upid;
                Document doc = Jsoup.connect(url)
                        .get();

                Elements titles = doc.select("td[class=pdtname]");

                boolean ok = false;
                if (titles.size() > 0) {
                    String title = titles.get(0).text().replace(",", "，");

                    Elements divs = doc.select("div[class=pdtInfo_02]");
                    if (divs.size() > 1) {
                        Elements values = divs.get(divs.size() - 1).select("td[class=way2]");
                        if (values.size() > 0) {
                            for (int i = values.size() - 1; i >= 0; i--) {
                                try {
                                    int price = Integer.parseInt(values.get(i).text().replace("元", "").replace(",", "，").trim());
                                    targetBw.write(upid + "," + price + "," + title + "\n");
                                    //System.out.println(pid + "," + price + "," + upid + "," + title);
                                    ok = true;
                                    break;
                                } catch (Exception e) {

                                }
                            }
                        }
                    }
                }
                if(!ok) {
                    targetBw.write(upid + ",0,\n");
                    System.out.println(upid + ",0,");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
