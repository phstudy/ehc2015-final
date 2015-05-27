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
 * Created by study on 5/27/15.
 */
public class FetchClass2 {
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c02.do?dc_cargxuid_0=";

    static BufferedReader noclassBr;
    static BufferedWriter classWb, failWb;

    public static void main(String[] args) throws Exception {
        int corePoolSize = 1;
        int maximumPoolSize = 1;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        noclassBr = FileManager.fileAsReader("noclass.csv");
        classWb = FileManager.fileAsWriter("class.csv");
        failWb = FileManager.fileAsWriter("failclass.csv");

        String line;
        while ((line = noclassBr.readLine()) != null) {
            if (cnt.intValue() % 10000 == 0) {
                classWb.flush();
                System.out.println(cnt.incrementAndGet());
            }
            pool.execute(new Task(line));
            cnt.incrementAndGet();
        }
        classWb.flush();
        failWb.flush();

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
            String pid = line;
            String upid = "U" + pid.substring(0, 9);
            String url = bashUrl + upid;

            try {
                Document doc = Jsoup.connect(url)
                        .get();

                Elements as = doc.select("a[itemprop=url]");
                if (as.size() > 0) {
                    String asurl = as.get(as.size() - 1).attr("href");
                    String cate = asurl.substring(asurl.indexOf("dc_cateid_0=") + "dc_cateid_0=".length());

                    classWb.write(pid + "," + cate + "\n");
                }
            } catch (Exception e) {
                try {
                    failWb.write(pid + "\n");
                } catch (Exception e1) {

                }
                //System.out.println(url);
                //e.printStackTrace();
            }

        }
    }

}
