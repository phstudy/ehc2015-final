package org.qty.mr.roc2;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.qty.validate.ValidationUtils;

public class EruidGroupMapper extends Mapper<LongWritable, Text, Text, NullWritable> {

    static enum LogType {
        VIEW, ORDER, DROP
    };

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, NullWritable>.Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        LogType type = getLogType(line);
        if (type == LogType.DROP) {
            return;
        }

        try {
            String plist = ValidationUtils.plist(line);
            if (plist == null) {
                return;
            }
            String[] productBuyList = plist.split(",");
            if (productBuyList.length % 3 != 0) {
                return;
            }

            for (int i = 0; i < productBuyList.length; i += 3) {
                String pid = productBuyList[i];
                context.write(new Text(pid), NullWritable.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected LogType getLogType(String log) {
        if (StringUtils.contains(log, "act=o")) {
            return LogType.ORDER;
        }
        return LogType.DROP;
    }

}
