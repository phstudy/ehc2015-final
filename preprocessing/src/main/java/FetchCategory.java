import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by study on 5/17/15.
 */
public class FetchCategory {
    static BufferedReader categoryOnlyBr;
    static BufferedWriter categoryBw;
    static AtomicInteger cnt = new AtomicInteger(0);
    static String bashUrl = "http://shopping.udn.com/mall/cus/cat/Cc1c01.do?dc_cateid_0=";
    static Set<String> cids = new HashSet<String>();

    public static void main(String[] args) throws Exception {
        int corePoolSize = 8;
        int maximumPoolSize = 20;
        long keepAliveTime = 60;
        int capacity = 100;

        ThreadPoolExecutor pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(capacity),
                new ThreadPoolExecutor.CallerRunsPolicy());

        //String categoryStr = "A_001_001_003_001";

        categoryOnlyBr = FileManager.fileAsReader("category_only.csv");
        categoryBw = FileManager.fileAsWriter("category_raw.csv");


        String line;
        while ((line = categoryOnlyBr.readLine()) != null) {
            if (cnt.getAndIncrement() % 10000 == 0) {
                categoryBw.flush();
                System.out.println(cnt.intValue());
            }
            pool.execute(new Task(line));
        }

        categoryBw.flush();
        categoryBw.close();
    }

    public static class Task implements Runnable {
        String line;

        public Task(String line) {
            this.line = line;
        }

        @Override
        public void run() {
            try {
                String categories[] = line.split(",");
                String part = categories[categories.length - 1];

                int idx = -1;
                do {
                    idx = part.lastIndexOf("_");

                    if (!cids.contains(part)) {
                        String url = bashUrl + part;

                        WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);
                        webClient.getOptions().setJavaScriptEnabled(false);
                        HtmlPage page = webClient.getPage(url);

                        List<HtmlAnchor> anchors = (List<HtmlAnchor>) page.getByXPath("/html/body//a[@itemprop='url']");

                        for (int i = 0; i < anchors.size(); i++) {
                            HtmlAnchor anchor = anchors.get(i);
                            String href = anchor.getHrefAttribute();

                            Iterator<DomElement> it = anchor.getChildElements().iterator();
                            while (it.hasNext()) {
                                DomElement span = it.next();
                                String categoryName = span.getTextContent().trim();
                                if (href.contains(bashUrl)) {
                                    String cid = href.replace(bashUrl, "");
                                    if (!cids.contains(cid)) {
                                        cids.add(cid);
                                        categoryBw.write(cid + "," + categoryName + "\n");
                                    }
                                }
                            }
                        }
                    }
                    if (idx > 0) {
                        part = part.substring(0, idx);
                    }
                } while (idx > 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
