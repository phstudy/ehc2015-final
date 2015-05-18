package ord.phstudy.ehc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by study on 5/18/15.
 */
public class DateGenerator {
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
