package org.qty.mr.roc1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.qty.QLabInitConfig;
import org.qty.file.FileManager;
import org.qty.validate.ValidationUtils;

public class TransformTask2 {
    public static void main(String[] args) throws Exception {
        HashSet<String> data = new HashSet<String>(FileUtils.readLines(new File(
                "/Users/qrtt1/test/ehc2015-final/preprocessing/src/main/resources/TRAINING_WITH_TOP20_ERUIDs")));

        BufferedWriter w = FileManager.fileAsWriter("ROC2App.log");
        for (String line : FileManager.fileAsLineIterator(QLabInitConfig.TRAIN_FILE)) {
            String eruid = ValidationUtils.eruid(line);
            if(data.contains(eruid)){
                w.write(line);
                w.write("\n");
            }
        }

    }
}
