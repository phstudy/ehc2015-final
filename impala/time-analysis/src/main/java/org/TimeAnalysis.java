package org;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * Created by study on 6/21/15.
 */
public class TimeAnalysis {
    private static final String CONNECTION_URL_PROPERTY = "connection.url";
    private static final String JDBC_DRIVER_NAME_PROPERTY = "jdbc.driver.class.name";

    private static String connectionUrl;
    private static String jdbcDriverName;


    private static void loadConfiguration() throws IOException {


        InputStream input = null;
        try {
            String filename = TimeAnalysis.class.getSimpleName() + ".conf";
            input = TimeAnalysis.class.getClassLoader().getResourceAsStream(filename);
            Properties prop = new Properties();
            prop.load(input);

            connectionUrl = prop.getProperty(CONNECTION_URL_PROPERTY);
            jdbcDriverName = prop.getProperty(JDBC_DRIVER_NAME_PROPERTY);
        } finally {
            try {
                if (input != null)
                    input.close();
            } catch (IOException e) {
                // nothing to do
            }
        }

    }

    public static void main(String[] args) throws IOException {
        FileWriter fw = new FileWriter(new File("/Users/study/Desktop/EHC/user_train_duration.csv"));


        int MAX_DURATION_PER_VIEW = 1 * 60 * 1000; // 3 minutes per view;

        String sqlStatement = "select * from ts_train order by eruid, ts asc";
        loadConfiguration();

        System.out.println("\n=============================================");
        System.out.println("Cloudera Impala JDBC Example");
        System.out.println("Using Connection URL: " + connectionUrl);
        System.out.println("Running Query: " + sqlStatement);

        Connection con = null;

        Set<String> robots = new HashSet<>();
        Map<String, Long> times = new HashMap<>();

        // train
        robots.add("30967eb11b");
        robots.add("aedd0f62d3");
        robots.add("a73c30a487");
        robots.add("c6cc02ffbf");
        robots.add("b653915ff9");
        robots.add("2226ddb59f");
        robots.add("943a628d4a");
        robots.add("53284bb584");
        robots.add("473ef604eb");
        robots.add("e23537e9d6");
        robots.add("88fc52931d");
        robots.add("c5cec2304f");
        robots.add("5b7f82f9b3");
        robots.add("08e6058168");
        robots.add("ce4e1d35bb");
        robots.add("eab7c164f6");
        robots.add("a3a7e4d2f5");
        robots.add("228aa7bd6c");
        robots.add("362b5d73bf");
        robots.add("d3387e78d1");
        robots.add("b6702c27cf");
        robots.add("30caf4ed3b");
        robots.add("2511c3e238");
        robots.add("9e9bf953e3");
        robots.add("2afdfb1c70");
        robots.add("0ba34dd5b9");
        robots.add("edfb217200");
        robots.add("a3a6737397");
        robots.add("d6e7d80894");
        robots.add("2e3df89628");
        robots.add("2533bc6d9c");
        robots.add("8a68beb18d");
        robots.add("eb167df198");
        robots.add("70269ad25d");
        robots.add("6d66d2ce78");
        robots.add("a7c726d7e7");
        robots.add("b641ebb015");
        robots.add("8d9afdaee9");
        robots.add("c1e5eb7d9f");
        robots.add("8beb18fff5");
        robots.add("54a47066a6");
        robots.add("d557d19a34");
        robots.add("2cb46de297");
        robots.add("f119b3e0ee");
        robots.add("c40b221697");
        robots.add("6a42983036");
        robots.add("7965d5a7dc");
        robots.add("87cef2042a");
        robots.add("93e6a0fad7");
        robots.add("681a55273d");
        robots.add("d4361e147f");
        robots.add("7e80a333fc");
        robots.add("94912fcb46");
        robots.add("ba0ede713f");
        robots.add("1cd643b3e3");
        robots.add("34c6538625");
        robots.add("abc91c148a");
        robots.add("2fc7a1756e");
        robots.add("79c4f6953a");
        robots.add("452f4b11b1");
        robots.add("2f8bcf79f1");
        robots.add("5f83b9826b");
        robots.add("568c8c0da7");

        // test
        robots.add("aedd0f62d3");
        robots.add("a73c30a487");
        robots.add("c6cc02ffbf");
        robots.add("74e86c4b3e");
        robots.add("b7dac81cb9");
        robots.add("561f379b29");
        robots.add("b653915ff9");
        robots.add("17776d1517");
        robots.add("943a628d4a");
        robots.add("3a95763f81");
        robots.add("84258e14a5");
        robots.add("473ef604eb");
        robots.add("e23537e9d6");
        robots.add("88fc52931d");
        robots.add("5b7f82f9b3");
        robots.add("ce4e1d35bb");
        robots.add("eab7c164f6");
        robots.add("12c8ef3a80");
        robots.add("362b5d73bf");
        robots.add("5b627dbc7f");
        robots.add("a306dc66c0");
        robots.add("30caf4ed3b");
        robots.add("2511c3e238");
        robots.add("9e9bf953e3");
        robots.add("2afdfb1c70");
        robots.add("5667afbc36");
        robots.add("ef12373e45");
        robots.add("0ba34dd5b9");
        robots.add("c6f2d7ca68");
        robots.add("720dd63471");
        robots.add("3f56719315");
        robots.add("a3a6737397");
        robots.add("1fd594f0df");
        robots.add("5bb5e59afb");
        robots.add("bac1868c14");
        robots.add("eb167df198");
        robots.add("d83d2d88cd");
        robots.add("70269ad25d");
        robots.add("6d66d2ce78");
        robots.add("4b28f531db");
        robots.add("4c7ccbb842");
        robots.add("de645e9947");
        robots.add("b641ebb015");
        robots.add("969ba4b2b1");
        robots.add("69453e7012");
        robots.add("44b1fac968");
        robots.add("8beb18fff5");
        robots.add("f119b3e0ee");
        robots.add("fec9a919d7");
        robots.add("7965d5a7dc");
        robots.add("6708e90d25");
        robots.add("d4361e147f");
        robots.add("7e80a333fc");
        robots.add("dee5b147c4");
        robots.add("1cfe642e6a");
        robots.add("34c6538625");
        robots.add("abc91c148a");
        robots.add("452f4b11b1");
        robots.add("347c1ae44b");

        try {

            Class.forName(jdbcDriverName);

            con = DriverManager.getConnection(connectionUrl);

            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery(sqlStatement);

            System.out.println("\n== Begin Query Results ======================");

            String exEruid = null;
            long exTime = 0L;
            long total = 0L;

            long counter = 0;

            long diffCounter = 0;
            long diffTotal = 0;

            long userTime = 0L;
            while (rs.next()) {
                String curEruid = rs.getString("eruid");

                String eruid = (curEruid.substring(9, 20) + curEruid.charAt(curEruid.length() - 1)).replace("-", "");

                if (robots.contains(eruid)) {
                    continue;
                }

                long curTime = rs.getTimestamp("ts").getTime();
                if (exEruid == null) {
                    exEruid = curEruid;
                    exTime = curTime;
                }

                long diff = curTime - exTime;

                if (curEruid != null && curEruid.equals(exEruid)) {
                    if (diff > 0) {
                        if (diff < MAX_DURATION_PER_VIEW) {
                            total += diff;
                            diffCounter++;
                            diffTotal += diff;
                            userTime += diff;
                        } else {
                            total += MAX_DURATION_PER_VIEW;
                            userTime += MAX_DURATION_PER_VIEW;
                        }
                    }
                }

                if (!curEruid.equals(exEruid)) {
                    total += MAX_DURATION_PER_VIEW;
                    userTime += MAX_DURATION_PER_VIEW;
                    times.put(exEruid, userTime);
                }

                exEruid = curEruid;
                exTime = curTime;
                counter++;
                if (counter % 1000000 == 0) {
                    System.out.println(new java.util.Date() + " " + counter);
                }
            }

            System.out.println(diffCounter); // count in range
            System.out.println(diffTotal / (double) diffCounter); // avg in range
            System.out.println(total / 1000.0); // secs
            System.out.println("== End Query Results =======================\n\n");

            times.forEach((key, val) -> {
                try {
                    fw.write(key + "," + val + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


            fw.flush();
            fw.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                // swallow
            }
        }
    }
}
