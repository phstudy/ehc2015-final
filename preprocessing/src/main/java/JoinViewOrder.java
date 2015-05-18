import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import callback.CSVProcessor;

import com.google.common.base.Joiner;
import file.FileManager;

public class JoinViewOrder {
    public static void main(String[] args) throws Exception {

        // key: erUid,pid, value: num
        final Map<String, Integer> erUidPidBuyNums = new HashMap<String, Integer>();

        new DataProcessor(FileManager.fileAsReader("order.csv")).process(new CSVProcessor<Map<String, Integer>>() {

            @Override
            public void process(String[] csv, Map<String, Integer> userData) throws Exception {
                // 0  , 1, 2,    3,  4,  5,    6
                // pid,ts,ip,price,num,uid,eruid
                userData.put(csv[6] + csv[0], Integer.valueOf(csv[4]));
            }
        }, erUidPidBuyNums);

        // 計算使用者看過同 1 個商品幾次
        final Map<String, Integer> erUidPidViewCount = new HashMap<String, Integer>();
        String viewInputFilename = "train_view.csv";

        new DataProcessor(FileManager.fileAsReader(viewInputFilename)).process(
                new CSVProcessor<Map<String, Integer>>() {

                    @Override
                    public void process(String[] csv, Map<String, Integer> writer) throws Exception {
                        //
                        //                qty:EHC qrtt1$ head view.csv
                        //                pid,ts,ip,uid,eruid
                        //                0005158462,1422720000000,203.145.207.188,,41ee27d6-5f83-b982-69f9-f378dc9fc11b
                        String key = csv[4] + "" + csv[0];
                        ArrayList<String> l = new ArrayList<String>();
                        l.addAll(Arrays.asList(csv));
                        if (!erUidPidViewCount.containsKey(key)) {
                            erUidPidViewCount.put(key, 0);
                        }
                        erUidPidViewCount.put(key, erUidPidViewCount.get(key) + 1);
                    }
                }, erUidPidViewCount);

        final Set<String> dataMergeViewHasProcessed = new HashSet<String>();
        new DataProcessor(FileManager.fileAsReader(viewInputFilename)).process(new CSVProcessor<Writer>() {

            @Override
            public void process(String[] csv, Writer writer) throws Exception {
                String key = csv[4] + "" + csv[0];

                if (dataMergeViewHasProcessed.contains(key)) {
                    return;
                }

                ArrayList<String> l = new ArrayList<String>();
                l.addAll(Arrays.asList(csv));

                // view count
                if (erUidPidViewCount.containsKey(key)) {
                    l.add("" + erUidPidViewCount.get(key));
                } else {
                    l.add("0");
                }

                writer.write(Joiner.on(",").join(l) + "\n");
                dataMergeViewHasProcessed.add(key);
            }
        }, FileManager.fileAsWriter("train_merge_view.csv"));

        final Set<String> dataHasProcessed = new HashSet<String>();
        new DataProcessor(FileManager.fileAsReader(viewInputFilename)).process(new CSVProcessor<Writer>() {

            @Override
            public void process(String[] csv, Writer writer) throws Exception {
                String key = csv[4] + "" + csv[0];

                if (dataHasProcessed.contains(key)) {
                    return;
                }

                ArrayList<String> l = new ArrayList<String>();
                l.addAll(Arrays.asList(csv));
                if (erUidPidBuyNums.containsKey(key)) {
                    l.add("1"); // 有買
                    l.add("" + erUidPidBuyNums.get(key)); // 數量 0
                } else {
                    l.add("0"); // 沒買
                    l.add("0"); // 數量 0
                }

                // view count
                if (erUidPidViewCount.containsKey(key)) {
                    l.add("" + erUidPidViewCount.get(key));
                } else {
                    l.add("0");
                }

                writer.write(Joiner.on(",").join(l) + "\n");
                dataHasProcessed.add(key);
            }
        }, FileManager.fileAsWriter("train_view_order.csv"));

    }

}
