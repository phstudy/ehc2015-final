package org.qty.mr;

import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

import com.google.common.base.Stopwatch;

public class EHCFinalProductApp extends Configured implements Tool {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public int run(String[] args) throws Exception {

        Stopwatch stopwatch = Stopwatch.createStarted();

        String input = args[0];
        String output = args[1];
        String tmpFile = "./tmp/" + System.currentTimeMillis();
        setConf(new Configuration());
        Configuration conf = getConf();

        Job job = Job.getInstance(conf, "qty app");
        job.setJarByClass(EHCFinalProductApp.class);
        job.setReducerClass(ProductPreprocessingReducer.class);
        job.setMapperClass(ProductPreprocessingMapper.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(ProductSession.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(tmpFile));

        job.waitForCompletion(true);
        System.out.println("consumed time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
        return 1;
    }

    public static void main(String[] args) throws Throwable {
        EHCFinalProductApp app = new EHCFinalProductApp();
        //        app.run(new String[] { "/Users/qrtt1/_ehc_final_data/small.log", "./foooo" });
        app.run(new String[] { "/Users/qrtt1/_ehc_final_data/EHC_2nd_round_train.log", "./foooo" });

    }
}
