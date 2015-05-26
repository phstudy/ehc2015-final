package org.phstudy.ehc.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by study on 5/18/15.
 */
public class WeekUtil {
    public static Map<String, String> weeks = new HashMap<String, String>();

    static {
        weeks.put("01/Feb", "Sun");
        weeks.put("02/Feb", "Mon");
        weeks.put("03/Feb", "Tue");
        weeks.put("04/Feb", "Wed");
        weeks.put("05/Feb", "Thu");
        weeks.put("06/Feb", "Fri");
        weeks.put("07/Feb", "Sat");
        weeks.put("08/Feb", "Sun");
        weeks.put("09/Feb", "Mon");
        weeks.put("10/Feb", "Tue");
        weeks.put("11/Feb", "Wed");
        weeks.put("12/Feb", "Thu");
        weeks.put("13/Feb", "Fri");
        weeks.put("14/Feb", "Sat");
        weeks.put("15/Feb", "Sun");
        weeks.put("16/Feb", "Mon");
        weeks.put("17/Feb", "Tue");
        weeks.put("18/Feb", "Wed");
        weeks.put("19/Feb", "Thu");
        weeks.put("20/Feb", "Fri");
        weeks.put("21/Feb", "Sat");
        weeks.put("22/Feb", "Sun");
        weeks.put("23/Feb", "Mon");
        weeks.put("24/Feb", "Tue");
        weeks.put("25/Feb", "Wed");
        weeks.put("26/Feb", "Thu");
        weeks.put("27/Feb", "Fri");
        weeks.put("28/Feb", "Sat");

        weeks.put("29/Feb", "Sun"); // ?
        weeks.put("30/Feb", "Mon"); // ?

        weeks.put("01/Mar", "Sun");
        weeks.put("02/Mar", "Mon");
        weeks.put("03/Mar", "Tue");
        weeks.put("04/Mar", "Wed");
        weeks.put("05/Mar", "Thu");
        weeks.put("06/Mar", "Fri");
        weeks.put("07/Mar", "Sat");
        weeks.put("08/Mar", "Sun");
        weeks.put("09/Mar", "Mon");
        weeks.put("10/Mar", "Tue");
        weeks.put("11/Mar", "Wed");
        weeks.put("12/Mar", "Thu");
        weeks.put("13/Mar", "Fri");
        weeks.put("14/Mar", "Sat");
        weeks.put("15/Mar", "Sun");
        weeks.put("16/Mar", "Mon");
        weeks.put("17/Mar", "Tue");
        weeks.put("18/Mar", "Wed");
        weeks.put("19/Mar", "Thu");
        weeks.put("20/Mar", "Fri");
        weeks.put("21/Mar", "Sat");
        weeks.put("22/Mar", "Sun");
        weeks.put("23/Mar", "Mon");
        weeks.put("24/Mar", "Tue");
        weeks.put("25/Mar", "Wed");
        weeks.put("26/Mar", "Thu");
        weeks.put("27/Mar", "Fri");
        weeks.put("28/Mar", "Sat");
        weeks.put("29/Mar", "Sun");
        weeks.put("30/Mar", "Mon");
        weeks.put("31/Mar", "Tue");
    }

    public static void main(String[] args) {
        Calendar cal = Calendar.getInstance(Locale.TAIWAN);
        cal.set(2015, Calendar.FEBRUARY, 1, 0, 0, 0);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.US);

        while (cal.get(Calendar.MONTH) < Calendar.APRIL) {
            String weekDay = dayFormat.format(cal.getTime());
            String month = monthFormat.format(cal.getTime());

            System.out.println(String.format("weeks.put(\"%02d", cal.get(Calendar.DATE)) + "/" + month + "\",\"" + weekDay + "\");");
            cal.add(Calendar.DATE, 1);
        }
    }
}
