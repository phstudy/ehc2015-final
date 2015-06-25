package org.qty.validate;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class TrainTop20EruidsAnswer {

    public static int size;
    public static Set<String> ANSWER_PIDS = Sets.newHashSet();

    static {

        try {
            LineIterator it = IOUtils.lineIterator(TrainTop20EruidsAnswer.class.getResourceAsStream("/TRAINING_TOP100"),
                    "utf-8");
            int count = 0;
            while (it.hasNext()) {
                ANSWER_PIDS.add(StringUtils.split(it.next(), "[ ,]+")[0]);
                count++;
                if (count == 20) {
                    break;
                }
            }
            size = ANSWER_PIDS.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean contains(String pid) {
        return ANSWER_PIDS.contains(pid);
    }

    public static void main(String[] args) {
        System.out.println(TrainTop20EruidsAnswer.ANSWER_PIDS.size());
    }

}
