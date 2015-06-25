package org.qty.validate;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class TrainTop20PidsAnswer {

    public static int size;
    public static Set<String> ERUIDs = Sets.newHashSet();

    static {

        try {
            LineIterator it = IOUtils.lineIterator(TrainTop20PidsAnswer.class.getResourceAsStream("/TRAINING_WITH_TOP20_ERUIDs"),
                    "utf-8");
            while (it.hasNext()) {
                ERUIDs.add(StringUtils.trimToEmpty(it.next()));
            }
            size = ERUIDs.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean contains(String eruid) {
        return ERUIDs.contains(eruid);
    }

    public static void main(String[] args) {
        System.out.println(TrainTop20PidsAnswer.ERUIDs.size());
    }

}
