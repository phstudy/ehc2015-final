import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Joiner;

public class JoinViewOrder {
    public static void main(String[] args) throws Exception {

        // key: erUid,pid, value: num
        final Map<String, Integer> erUidPidNums = new HashMap<String, Integer>();

        new DataProcessor(FileManager.fileAsReader("order.csv")).process(new CSVProcessor<Map<String, Integer>>() {

            @Override
            public void process(String[] csv, Map<String, Integer> userData) throws Exception {
                // 0  , 1, 2,    3,  4,  5,    6
                // pid,ts,ip,price,num,uid,eruid
                userData.put(csv[6] + csv[0], Integer.valueOf(csv[4]));
            }
        }, erUidPidNums);

        new DataProcessor(FileManager.fileAsReader("view.csv")).process(new CSVProcessor<Writer>() {

            @Override
            public void process(String[] csv, Writer writer) throws Exception {
                //
                //                qty:EHC qrtt1$ head view.csv
                //                pid,ts,ip,uid,eruid
                //                0005158462,1422720000000,203.145.207.188,,41ee27d6-5f83-b982-69f9-f378dc9fc11b
                String key = csv[4] + "" + csv[0];
                ArrayList<String> l = new ArrayList<String>();
                l.addAll(Arrays.asList(csv));
                if (erUidPidNums.containsKey(key)) {
                    l.add("1"); // 有買
                    l.add("" + erUidPidNums.get(key)); // 數量 0
                } else {
                    l.add("0"); // 沒買
                    l.add("0"); // 數量 0
                }

                writer.write(Joiner.on(",").join(l) + "\n");
            }
        }, FileManager.fileAsWriter("train_view_order.csv"));

    }

}
