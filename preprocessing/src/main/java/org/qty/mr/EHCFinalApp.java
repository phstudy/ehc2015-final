package org.qty.mr;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

public class EHCFinalApp extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {

        for (String s : args) {
            System.out.println("arg: " + s);
        }

        long t1 = System.currentTimeMillis();

        // 決定要跑 user data 或 product data 的前處理
        String type = args[0];

        String input = args[1];
        String output = args[2];
        setConf(new Configuration());
        Configuration conf = getConf();

        Job job = buildJob(conf, type);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));
        job.waitForCompletion(true);
        long t2 = System.currentTimeMillis();
        System.out.println("consumed time: " + ((t2 - t1) / 1000) + " seconds");
        return 1;
    }

    protected Job buildJob(Configuration conf, String type) throws IOException {
        Job job = Job.getInstance(conf, "qty app");
        job.setJarByClass(EHCFinalApp.class);
        job.setMapOutputKeyClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        if ("user".equals(type)) {
            job.setReducerClass(UserPreprocessingReducer.class);
            job.setMapperClass(UserPreprocessingMapper.class);
            job.setMapOutputValueClass(UserSession.class);
        } else if ("product".equals(type)) {
            job.setReducerClass(ProductPreprocessingReducer.class);
            job.setMapperClass(ProductPreprocessingMapper.class);
            job.setMapOutputValueClass(ProductSession.class);
        } else {
            throw new IllegalArgumentException("type should be \"user\" or \"product\"");
        }
        return job;
    }

    public static void main(String[] args) throws Throwable {
        EHCFinalApp app = new EHCFinalApp();
        app.run(args);

    }
}
