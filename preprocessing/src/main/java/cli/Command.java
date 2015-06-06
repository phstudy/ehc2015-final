package cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qty.ProductData;
import org.qty.UserData;
import org.qty.file.FileManager;
import org.qty.validate.ReadAndCheck_V5_BuyByPredictBuyCount;
import org.qty.validate.ReadAndCheck_V6_BuyByPredictBuyCountGA;

public class Command {

    private static Map<String, String> cmdDesc = new HashMap<String, String>();
    static {
        cmdDesc.put("user", "轉換 log 成 user data");
        cmdDesc.put("product", "轉換 log 成 product data");
        cmdDesc.put("where", "查詢工作區的檔案絕對路徑");
        cmdDesc.put("result5", "不知不覺亂寫到了第 5 版");
        cmdDesc.put("result6", "不知不覺亂寫到了第 6 版");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            dumpCommands();
            System.exit(0);
        }

        if (!cmdDesc.containsKey(args[0])) {
            dumpCommands();
            System.exit(0);
        }

        // args 0 是 Class 名稱，後面的是要給它的參數

        try {
            List<String> realArgs = Arrays.asList(args);
            String[] aArgs = realArgs.subList(1, realArgs.size()).toArray(new String[0]);

            if ("user".equals(args[0])) {
                UserData.main(aArgs);
                return;
            }

            if ("product".equals(args[0])) {
                ProductData.main(aArgs);
                return;
            }

            if ("where".equals(args[0])) {
                System.out.println(FileManager.file(args[1]).getAbsolutePath());
                return;
            }

            if ("result5".equals(args[0])) {
                ReadAndCheck_V5_BuyByPredictBuyCount.main(aArgs);
                return;
            }
            
            if ("result6".equals(args[0])) {
                ReadAndCheck_V6_BuyByPredictBuyCountGA.main(aArgs);
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void dumpCommands() {
        for (String name : cmdDesc.keySet()) {
            System.out.println(String.format("%-10s \t%s", name, cmdDesc.get(name)));
        }
    }

}
