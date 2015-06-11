package org.qty.mr;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ProductPreprocessingReducer extends Reducer<Text, ProductSession, NullWritable, Text> {

    @Override
    protected void reduce(Text key, Iterable<ProductSession> values,
            Reducer<Text, ProductSession, NullWritable, Text>.Context context) throws IOException, InterruptedException {

        String cat = "NA";
        ProductSession bigSession = null;
        for (ProductSession productSession : values) {
            if (!"NA".equals(productSession.cate)) {
                cat = productSession.cate;
            }

            if (bigSession == null) {
                bigSession = productSession;

                continue;
            }

            bigSession.join(productSession);
            productSession.invalidate();
        }

        if ("NA".equals(cat)) {
            // 在 order 有出現商品，但在 view 沒有，所以不知道 cat 
            // 忽略它
            return;
        }

        bigSession.setCategory(cat);
        context.write(NullWritable.get(), new Text(bigSession.toString()));
        bigSession.invalidate();
    }
}