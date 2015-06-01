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
public class FetchChild5 {
    static BufferedReader sourceBr;
    static BufferedWriter targetBw;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c10.do?dc_btn_0=Func_FormalPreview&dc_cargxuid_0=";

    static Set<String> pids = Sets.newConcurrentHashSet();

    public static void main(String[] args) throws Exception {
        int corePoolSize = 2;
        int maximumPoolSize = 2;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        sourceBr = FileManager.fileAsReader("TEST_ALREADY_GUESS");
        targetBw = FileManager.fileAsWriter("TEST_ALREADY_GUESS_HAS_CHILD");

        String line;

        while ((line = sourceBr.readLine()) != null) {
            if (cnt.intValue() % 1000 == 0) {
                targetBw.flush();
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
            String pid = line;
            if (pid.length() == 13) {
                return;
            }

            //String upid = parts[2];
            //String title = "";
            //String price = parts[1];

//            if (parts.length > 3) {
//                title = parts[3];
//            }

            if (pids.contains(pid)) {
                return;
            } else {
                pids.add(pid);
            }

            try {
                String upid = pid.substring(0, 9);

                String url = bashUrl + "U" + upid;
                Document doc = Jsoup.connect(url)
                        .get();

                String title = "";
                int price = 0;

                Elements titles = doc.select("td[class=pdtname]");

                if (titles.size() > 0) {
                    title = titles.get(0).text().replace(",", "，");

                    Elements divs = doc.select("div[class=pdtInfo_02]");
                    if (divs.size() > 1) {
                        Elements values = divs.get(divs.size() - 1).select("td[class=way2]");
                        if (values.size() > 0) {
                            for (int i = values.size() - 1; i >= 0; i--) {
                                try {
                                    price = Integer.parseInt(values.get(i).text().replace("元", "").replace(",", "，").trim());
                                    break;
                                } catch (Exception e) {

                                }
                            }
                        }
                    }
                }


                Elements select = doc.select("select[id=dc_specxuid_0]");

                if (select.size() > 0) {
                    Elements options = select.get(0).select("option");
                    for (int i = 0; i < options.size(); i++) {
                        options.get(i).attr("value");

                        String cpid = options.get(i).attr("value");
                        cpid = cpid.substring(1);
                        int check = 0;
                        for (int j = 0; j < 9; j++) {
                            check += cpid.charAt(j) - '0';
                        }

                        check %= 10;
                        cpid += check;

                        String type = options.get(i).text();

                        targetBw.write(cpid + "," + upid + "," + price + "," + title + "," + type + "\n");
                        //System.out.println(pid + "," + price + "," + upid + "," + title);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
