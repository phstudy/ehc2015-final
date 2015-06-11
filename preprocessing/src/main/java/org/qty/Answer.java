package org.qty;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

public class Answer {

    public static Set<String> getPidSet() throws IOException {
        Set<String> s = new HashSet<String>();
        for (String line : IOUtils.readLines(Answer.class.getResourceAsStream("/ANSWER"))) {
            line = StringUtils.substringAfter(line, ",");
            line = StringUtils.substringBefore(line, " ");
            s.add(StringUtils.strip(line));
        }
        return s;
    }

    public static void main(String[] args) throws IOException {
        Answer.getPidSet();
    }
}
