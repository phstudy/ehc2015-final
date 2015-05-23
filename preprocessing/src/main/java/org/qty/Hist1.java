package org.qty;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;

import file.FileManager;

public class Hist1 {

    private static String inputFile = "EHC_2nd_round_train.log";

    static int time(String line) {
        String s = StringUtils.substringBetween(line, "[", " +0800]");
        String hour =  StringUtils.substringBetween(s, ":", ":");
        return NumberUtils.toInt(hour);
    }

    public static void main(String[] args) throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (String s : FileManager.fileAsLineIterator(inputFile)) {
            if (!s.contains("act=o")) {
                continue;
            }
            System.out.println(time(s));
        }
    }

}
