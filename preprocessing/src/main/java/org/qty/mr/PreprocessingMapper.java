package org.qty.mr;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.qty.validate.ValidationUtils;

import com.google.common.base.Optional;

public class PreprocessingMapper extends Mapper<LongWritable, Text, Text, UserSession> {

    static enum LogType {
        VIEW, ORDER, DROP
    };

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, UserSession>.Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        LogType type = getLogType(line);
        if (type == LogType.DROP) {
            return;
        }

        String eruid = ValidationUtils.eruid(line);
        UserSession session = new UserSession(eruid);
        if (type == LogType.ORDER) {
            session.buy();
        }

        if (type == LogType.VIEW) {
            String cat = getCategoryKey(line);
            String pid = ValidationUtils.pid(line);
            session.viewProduct(cat, pid);
        }

        context.write(new Text(session.toGroupKey()), session);
    }

    protected LogType getLogType(String log) {
        if (StringUtils.contains(log, "act=o")) {
            return LogType.ORDER;
        }
        if (StringUtils.contains(log, "act=v")) {
            return LogType.VIEW;
        }
        return LogType.DROP;
    }

    static String getCategoryKey(String line) {
        try {
            String s = StringUtils.substringBetween(line, "cat=", ";");
            return Optional.fromNullable("" + s.charAt(0)).or("_");
        } catch (Exception e) {
            return "_";
        }
    }
}
