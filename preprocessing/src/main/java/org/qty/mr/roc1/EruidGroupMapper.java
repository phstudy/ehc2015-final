package org.qty.mr.roc1;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.qty.validate.TrainTop20PidsAnswer;
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

        String eruid = ValidationUtils.eruid(line);
        String pid = ValidationUtils.pid(line);
        if (TrainTop20PidsAnswer.contains(pid)) {
            context.write(new Text(eruid), NullWritable.get());
        }
    }

    protected LogType getLogType(String log) {
        if (StringUtils.contains(log, "act=v")) {
            return LogType.VIEW;
        }
        return LogType.DROP;
    }

}
