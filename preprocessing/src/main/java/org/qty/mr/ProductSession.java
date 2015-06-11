package org.qty.mr;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Writable;

import com.google.common.base.Joiner;

public class ProductSession implements Writable {

    String pid;
    int buyCount = 0;
    String cate = "NA";
    Map<String, Integer> viewHistory = new HashMap<String, Integer>();

    protected ProductSession() {
    }

    public ProductSession(String pid) {
        this.pid = pid;
    }

    public void view(String eruid) {
        if (StringUtils.isEmpty(eruid)) {
            return;
        }
        if (!viewHistory.containsKey(eruid)) {
            viewHistory.put(eruid, 1);
            return;
        }
        int count = viewHistory.get(eruid);
        viewHistory.put(eruid, count + 1);
    }

    public void buy(int num) {
        buyCount += num;
    }

    public void setCategory(String cat) {
        this.cate = cat;
    }

    static Log logger = LogFactory.getLog(ProductSession.class);

    public synchronized void join(ProductSession other) {
        if (!this.pid.equals(other.pid)) {
            return;
        }

        this.buyCount += other.buyCount;
        if ("NA".equals(this.cate) && !"NA".equals(other.cate)) {
            this.cate = other.cate;
        }

        for (String eruid : other.viewHistory.keySet()) {
            if (!this.viewHistory.containsKey(eruid)) {
                viewHistory.put(eruid, other.viewHistory.get(eruid));
                continue;
            }
            viewHistory.put(eruid, other.viewHistory.get(eruid) + viewHistory.get(eruid));
        }

    }

    public String toString() {
        ArrayList<String> list = new ArrayList<String>();
        //pid,view,viewBySession,price,cat,buyCount
        int allViewed = 0;
        for (Integer v : viewHistory.values()) {
            allViewed += v;
        }

        list.add(pid);
        list.add("" + allViewed);
        list.add("" + viewHistory.size());
        list.add("" + ReadOnlyPriceData.lookUp(pid));
        list.add("" + (int) cate.charAt(0));
        list.add("" + buyCount);
        return Joiner.on(",").join(list);
    }

    public void invalidate() {
        viewHistory.clear();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(pid);
        out.writeInt(buyCount);
        out.writeUTF(cate);

        out.writeInt(viewHistory.size());
        for (String e : viewHistory.keySet()) {
            out.writeUTF(e);
            out.writeInt(viewHistory.get(e));
        }

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        pid = in.readUTF();
        buyCount = in.readInt();
        cate = in.readUTF();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            viewHistory.put(in.readUTF(), in.readInt());
        }
    }

}
