package org.qty.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Writable;

import com.google.common.base.Joiner;

public class UserSession implements Writable {

    public final static String[] CAT_NAMES = "0,1,A,B,C,D,E,F,G,H,I,J,K,L,O,V".split(",");
    public final static String PREFIX = "user_";

    private String eruid;
    private int buyCount = 0;
    private List<String> viewPidHistory = new ArrayList<String>();
    private Map<String, Integer> catStat = new HashMap<String, Integer>();

    public String toGroupKey() {
        return PREFIX + eruid;
    }

    public static boolean inGroup(String key) {
        return StringUtils.startsWith(key, PREFIX);
    }

    private UserSession() {
    }

    public UserSession(String eruid) {
        this.eruid = eruid;
    }

    public void viewProduct(String cat, String pid) {
        viewPidHistory.add(pid);
        viewCate(cat);
    }

    private void viewCate(String cat) {
        if (!catStat.containsKey(cat)) {
            catStat.put(cat, 1);
            return;
        }
        catStat.put(cat, catStat.get(cat) + 1);
    }

    public void buy() {
        buyCount += 1;
    }

    public String getEruid() {
        return eruid;
    }

    public void join(UserSession other) {
        if (!other.getEruid().equals(eruid)) {
            return;
        }

        this.buyCount += other.buyCount;
        this.viewPidHistory.addAll(other.viewPidHistory);

        for (Entry<String, Integer> stat : other.catStat.entrySet()) {
            if (!this.catStat.containsKey(stat.getKey())) {
                this.catStat.put(stat.getKey(), stat.getValue());
                continue;
            }

            this.catStat.put(stat.getKey(), this.catStat.get(stat.getKey()) + stat.getValue());
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(eruid);
        out.writeInt(buyCount);
        out.writeInt(catStat.size());
        for (Entry<String, Integer> e : catStat.entrySet()) {
            out.writeUTF(e.getKey());
            out.writeInt(e.getValue());
        }
        out.writeInt(viewPidHistory.size());
        for (String s : viewPidHistory) {
            out.writeUTF(s);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        eruid = in.readUTF();
        buyCount = in.readInt();

        int catStatSize = in.readInt();
        for (int i = 0; i < catStatSize; i++) {
            catStat.put(in.readUTF(), in.readInt());
        }

        int historySize = in.readInt();
        for (int i = 0; i < historySize; i++) {
            viewPidHistory.add(in.readUTF());
        }
    }

    public void invalidate() {
        this.eruid = null;
        this.catStat.clear();
        this.viewPidHistory.clear();
    }

    @Override
    public String toString() {
        // output 
        // eruid,viewcount,uniq_viewcount,
        // cat_0,cat_1,cat_A,cat_B,cat_C,cat_D,cat_E,cat_F,cat_G,cat_H,cat_I,cat_J,cat_K,cat_L,cat_O,cat_V,
        // max_cat,buyCount,buy
        //
        ArrayList<String> list = new ArrayList<String>();
        list.add(eruid);
        list.add("" + viewPidHistory.size());
        list.add("" + new HashSet<String>(viewPidHistory).size());

        int maxCatCount = 0;
        String maxCatKey = "_";
        for (String cate : CAT_NAMES) {
            int count = 0;
            if (catStat.containsKey(cate)) {
                count = catStat.get(cate);
            }

            if (count > maxCatCount) {
                maxCatCount = count;
                maxCatKey = cate;
            }
            list.add("" + count);
        }
        list.add(maxCatKey);
        list.add("" + buyCount);
        list.add(buyCount > 0 ? "1" : "0");
        return Joiner.on(",").join(list);
    }

}
