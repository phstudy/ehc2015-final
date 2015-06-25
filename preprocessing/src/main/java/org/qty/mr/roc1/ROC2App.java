package org.qty.mr.roc1;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class ROC2App extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {

        for (String s : args) {
            System.out.println("arg: " + s);
        }

        long t1 = System.currentTimeMillis();

        String input = "/Users/qrtt1/EHC/EHC_2nd_round_train.log";
        String output = "./tmp/" + System.currentTimeMillis();
        setConf(new Configuration());
        Configuration conf = getConf();

        Job job = buildJob(conf, null);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        job.waitForCompletion(true);
        long t2 = System.currentTimeMillis();
        System.out.println("consumed time: " + ((t2 - t1) / 1000) + " seconds");
        return 1;
    }

    protected Job buildJob(Configuration conf, String type) throws IOException {
        Job job = Job.getInstance(conf, "qty app");
        job.setJarByClass(ROC2App.class);
        job.setMapOutputKeyClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setReducerClass(EruidWithTop20OnlyReducer.class);
        job.setMapperClass(EruidGroupMapper.class);
        job.setMapOutputValueClass(NullWritable.class);
        return job;
    }

    public static void main(String[] args) throws Throwable {
        ROC2App app = new ROC2App();
        app.run(args);
        // app.run(new String[] { "product",
        // "/Users/qrtt1/_ehc_final_data/EHC_2nd_round_train.log",
        // "tmp/" + System.currentTimeMillis() });
        // app.run(new String[] { "user",
        // "/Users/qrtt1/_ehc_final_data/EHC_2nd_round_train.log",
        // "tmp/" + System.currentTimeMillis() });

    }
}
