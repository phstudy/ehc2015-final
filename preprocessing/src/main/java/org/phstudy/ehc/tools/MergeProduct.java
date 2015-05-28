package org.phstudy.ehc.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

import org.qty.file.FileManager;

/**
 * Created by study on 5/26/15.
 */
public class MergeProduct {
    public static void main(String[] args) throws Exception {
        BufferedReader product1Br = FileManager.fileAsReader("products_full_v1.csv");
        BufferedReader product2Br = FileManager.fileAsReader("products_full_v2.csv");

        BufferedWriter productWb = FileManager.fileAsWriter("products_full.csv");

        Map<String, Rec> map = new HashMap<String, Rec>();

        String line;

        while ((line = product1Br.readLine()) != null) {
            String[] str = line.split(",");

            String pid = str[0];
            Rec rec;
            if (map.containsKey(pid)) {
                rec = map.get(pid);
            } else {
                rec = new Rec();
                map.put(pid, rec);
            }
            if(str.length > 3) { //pid,price,upid,desc
                rec.pid = str[0];
                rec.price = str[1];
                rec.upid = str[2];
                rec.desc = str[3];
            } else  {
                rec.pid = str[0];
                rec.price = str[1];
                rec.upid = str[2];
            }
        }

        while ((line = product2Br.readLine()) != null) {
            String[] str = line.split(",");

            String pid = str[0];
            Rec rec;
            if (map.containsKey(pid)) {
                rec = map.get(pid);
            } else {
                rec = new Rec();
                map.put(pid, rec);
            }
            if(str.length > 3) { //pid,price,upid,desc
                rec.pid = str[0];
                rec.price = str[1];
                rec.upid = str[2];
                rec.desc = str[3];
            } else  {
                rec.pid = str[0];
                rec.price = str[1];
                rec.upid = str[2];
            }
        }

        map.forEach((key, val) -> {
            try {
                productWb.write(val.pid + "," + val.price + "," + val.upid + "," + val.desc + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        productWb.flush();
        productWb.close();
    }

    public static class Rec {
        public String pid;
        public String upid;
        public String desc = "";
        public String price = "0";
    }
}
