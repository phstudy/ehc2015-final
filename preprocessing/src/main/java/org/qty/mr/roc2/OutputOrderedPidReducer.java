package org.qty.mr.roc2;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class OutputOrderedPidReducer extends Reducer<Text, NullWritable, NullWritable, Text> {

    @Override
    protected void reduce(Text key, Iterable<NullWritable> values,
            Reducer<Text, NullWritable, NullWritable, Text>.Context context) throws IOException, InterruptedException {

        context.write(NullWritable.get(), key);
    }

}