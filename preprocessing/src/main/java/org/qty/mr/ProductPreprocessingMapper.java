package org.qty.mr;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.qty.validate.NetworkPriceFetcher;
import org.qty.validate.ValidationUtils;

import com.google.common.base.Optional;

public class ProductPreprocessingMapper extends Mapper<LongWritable, Text, Text, ProductSession> {

    static enum LogType {
        VIEW, ORDER, DROP
    };

    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, ProductSession>.Context context)
            throws IOException, InterruptedException {

        String line = value.toString();
        LogType type = getLogType(line);
        if (type == LogType.DROP) {
            return;
        }

        String eruid = ValidationUtils.eruid(line);
        if (StringUtils.isEmpty(eruid)) {
            return;
        }

        if (type == LogType.VIEW) {
            //124.8.72.129 - - [01/Feb/2015:00:00:01 +0800] "GET /action?;act=view;uid=;pid=0004539894;cat=J,J_007,J_007_002,J_007_002_001;erUid=d3fcfdc2-3a14-8cf4-92d0-10247e4883f1; HTTP/1.1" 302 160 "-" "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36"
            String pid = ValidationUtils.pid(line);
            String cat = getCategoryKey(line);
            ProductSession session = new ProductSession(pid);
            session.view(eruid);
            session.setCategory(cat);
            context.write(new Text(pid), session);
            return;
        }

        if (type == LogType.ORDER) {
            // 114.41.4.218 - - [01/Feb/2015:00:00:01 +0800] "GET /action?;act=order;uid=U312622727;plist=0006944501,1,1069;erUid=252b97f1-25bd-39ea-6006-3f3ebf52c80; HTTP/1.1" 302 160 "-" "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; WOW64; Trident/6.0; MAARJS)"
            String s = StringUtils.substringBetween(line, "plist=", ";");
            if (StringUtils.isBlank(s)) {
                return;
            }
            try {
                String[] productBuyList = ValidationUtils.plist(line).split(",");
                if (productBuyList.length % 3 != 0) {
                    return;
                }
                for (int i = 0; i < productBuyList.length; i += 3) {
                    String pid = productBuyList[i];
                    int num = NumberUtils.toInt(productBuyList[i + 1], 1);
                    ProductSession session = new ProductSession(pid);
                    session.buy(num);
                    context.write(new Text(pid), session);
                }

                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
