package org.qty.mr.roc1;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class EruidWithTop20OnlyReducer extends Reducer<Text, LogGroup, NullWritable, Text> {

    @Override
    protected void reduce(Text key, Iterable<LogGroup> values,
            Reducer<Text, LogGroup, NullWritable, Text>.Context context) throws IOException, InterruptedException {

        context.write(NullWritable.get(), key);
    }

}