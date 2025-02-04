package org.qty.validate;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

public class TestAnswer {

    public static int size;
    public static Set<String> ANSWER_PIDS = Sets.newHashSet();

    static {

        try {
            LineIterator it = IOUtils.lineIterator(TestAnswer.class.getResourceAsStream("/TEST_ANSWER"), "utf-8");
            while (it.hasNext()) {
                ANSWER_PIDS.add(StringUtils.split(it.next(), ",")[0]);
            }
            size = ANSWER_PIDS.size();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
