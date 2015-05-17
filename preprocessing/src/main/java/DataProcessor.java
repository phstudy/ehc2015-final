import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import domain.Record;

public class DataProcessor {

    private BufferedReader reader;
    String regex = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"GET /action\\?;(.+?) HTTP/1.(\\d+)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
    Pattern pattern = Pattern.compile(regex);
    static SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public DataProcessor(BufferedReader reader) {
        this.reader = reader;
    }

    public <T> void process(CSVProcessor<T> processor, T userData) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            try {
                processor.process(line.split(","), userData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public <T> void process(LogProcessor<T> processor, T userData) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (!matcher.find()) {
                continue;
            }

            String ip = matcher.group(1);
            Date ts = null;
            try {
                ts = sdf.parse(matcher.group(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Map<String, Object> data = Utility.requestToMap(matcher.group(5));
            int code = Integer.parseInt(matcher.group(7));
            int bytes = Integer.parseInt(matcher.group(8));
            String referer = matcher.group(9);

            //UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
            //ReadableUserAgent agent = parser.parse(matcher.group(10));
            try {
                processor.process(new Record(null, ip, ts, data, code, bytes, referer), userData);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
