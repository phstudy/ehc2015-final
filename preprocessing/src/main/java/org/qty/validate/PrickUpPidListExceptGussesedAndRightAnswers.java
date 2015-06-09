package org.qty.validate;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * show 出 predict list 內除了已猜過與正確答案的部分
 * 
 * @author qrtt1
 */
public class PrickUpPidListExceptGussesedAndRightAnswers {

    public static void main(String[] args) throws IOException {
        List<String> alreadyGuess = IOUtils.readLines(
                PrickUpPidListExceptGussesedAndRightAnswers.class.getResourceAsStream("/TEST_ALREADY_GUESS"), "utf-8");
        alreadyGuess.addAll(TestAnswer.ANSWER_PIDS);

        for (String line : IOUtils.readLines(
                PrickUpPidListExceptGussesedAndRightAnswers.class.getResourceAsStream("/predict_list.txt"), "utf-8")) {
            String[] data = line.split(",");
            if (!alreadyGuess.contains(data[1])) {
                System.out.println(data[1]);
            }
        }
    }
}
