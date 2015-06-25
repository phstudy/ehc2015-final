package org.qty.mr.roc1;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;

public class LogGroup implements Writable {

    String eruid;
    List<String> log = new ArrayList<String>();

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(eruid);
        out.writeInt(log.size());
        for (String s : log) {
            out.writeUTF(s);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        eruid = in.readUTF();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            log.add(in.readUTF());
        }
    }
    
    

}
