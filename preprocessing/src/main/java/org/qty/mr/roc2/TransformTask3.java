package org.qty.mr.roc2;

import java.io.BufferedWriter;
import java.io.File;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.qty.QLabInitConfig;
import org.qty.file.FileManager;
import org.qty.validate.ValidationUtils;

public class TransformTask3 {
    public static void main(String[] args) throws Exception {
        HashSet<String> data = new HashSet<String>(FileUtils.readLines(new File(
                "/Users/qrtt1/test/ehc2015-final/preprocessing/src/main/resources/ORDERED_PIDs")));

        BufferedWriter w = FileManager.fileAsWriter("ROC3App.log");
        for (String line : FileManager.fileAsLineIterator(QLabInitConfig.TRAIN_FILE)) {
            if (line.contains("act=v")) {
                String pid = ValidationUtils.pid(line);
                if (data.contains(pid)) {
                    w.write(line);
                    w.write("\n");
                }
            } else {
                w.write(line);
                w.write("\n");
            }
        }

    }
}
