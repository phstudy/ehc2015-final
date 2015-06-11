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

public class ProductSession implements Writable {

    String pid;
    int buyCount = 0;
    String cate = "NA";
    List<String> viewHistory = new ArrayList<String>();

    protected ProductSession() {
    }

    public ProductSession(String pid) {
        this.pid = pid;
    }

    public void view(String eruid) {
        if (StringUtils.isEmpty(eruid)) {
            return;
        }
        viewHistory.add(eruid);
    }

    public void buy(int num) {
        buyCount += num;
    }

    public void setCategory(String cat) {
        this.cate = cat;
    }

    public void join(ProductSession other) {
        if (!this.pid.equals(other.pid)) {
            return;
        }

        this.buyCount += other.buyCount;
        if ("NA".equals(this.cate) && !"NA".equals(other.cate)) {
            this.cate = other.cate;
        }
        this.viewHistory.addAll(other.viewHistory);
    }

    public String toString() {
        ArrayList<String> list = new ArrayList<String>();
        //pid,view,viewBySession,price,cat,buyCount
        list.add(pid);
        list.add("" + viewHistory.size());
        list.add("" + new HashSet<String>(viewHistory).size());
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
        for (String e : viewHistory) {
            out.writeUTF(e);
        }

    }

    @Override
    public void readFields(DataInput in) throws IOException {
        pid = in.readUTF();
        buyCount = in.readInt();
        cate = in.readUTF();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            viewHistory.add(in.readUTF());
        }
    }

}
