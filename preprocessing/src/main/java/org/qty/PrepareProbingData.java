package org.qty;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Joiner;

public class PrepareProbingData {

    public static void main(String[] args) throws Exception {
        InputStream input = PrepareProbingData.class.getResourceAsStream("/traindata_hadoop_stat.txt");
        Iterator<String> it = IOUtils.lineIterator(input, "utf-8");
        Map<String, Long> data = new HashMap<String, Long>();
        while (it.hasNext()) {
            String s = it.next();
            String[] kv = s.split("[ \t]+");
            data.put(kv[0], Long.valueOf(kv[1]));
        }

        List<Entry<String, Long>> list = new ArrayList<Entry<String, Long>>(data.entrySet());
        Collections.sort(list, new Comparator<Entry<String, Long>>() {

            @Override
            public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                return (int) (o2.getValue() - o1.getValue());
            }
        });

        new File("prober").mkdirs();
        for (int i = 0; i < 100; i += 20) {
            System.out.println(i);
            int startIndex = i;
            int endIndex = startIndex + 20;

            ArrayList<String> fileContent = new ArrayList<String>();
            int rank = 1;
            for (Entry<String, Long> entry : list.subList(startIndex, endIndex)) {
                fileContent.add(String.format("%02d,%s", rank, entry.getKey()));
                rank++;
            }

            Writer writer = new FileWriter(String.format("prober/%03d_%03d.txt", startIndex, endIndex));
            writer.write(Joiner.on("\n").join(fileContent));
            writer.close();
        }

    }

}
