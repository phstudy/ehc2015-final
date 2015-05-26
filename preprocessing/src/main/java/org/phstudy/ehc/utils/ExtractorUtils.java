package org.phstudy.ehc.utils;

/**
 * Created by study on 5/19/15.
 */
public class ExtractorUtils {
    // magic pos
    final static int MIN_HOUR_COLON_POS = 25;
    final static int MIN_IP_SPACE_POS = 8;
    final static int MIN_USER_AGENT_SPACE_POS = 106;
    final static int MIN_PID_POS = 70;
    final static int MIN_CATEGORY_POS = 85;
    final static int MIN_ERUID_POS = 78;
    final static int MIN_UID_POS = 56;
    final static int MIN_PLIST_POS = 71;
    final static int MIN_ETUREC_POS = 100;
    final static String DEVICIES_STR[] = {"iPhone", "iPad", "Android"};
    final static char DEVICIES[] = {'A', 'B', 'C', 'D'};

    // A:iPhone
    // B:iPad
    // C:Android
    // D:Other

    public static String extractCategory(String line) {
//        int catIdx = line.indexOf("cat=", MIN_CATEGORY_POS);
//        String cids = "";
//        if (catIdx != -1) {
//            int endCatIdx = line.indexOf(";", catIdx + 4);
//            cids = line.substring(catIdx + 4, endCatIdx);
//            if (cids.charAt(1) == '_') {
//                String[] cparts = cids.split("_");
//
//                StringBuilder sb = new StringBuilder(49);
//                StringBuilder sb2 = new StringBuilder(17);
//                sb2.append(cparts[0]);
//                sb.append(cparts[0]);
//                for (int i = 1; i < cparts.length; i++) {
//                    sb2.append("_").append(cparts[i]);
//                    sb.append(",").append(sb2);
//                }
//                for (int i = cparts.length; i <= 4; i++) {
//                    sb.append(",");
//                }
//                cids = sb.toString();
//            } else {
//                int commaCount = CharMatcher.is(',').countIn(cids);
//                cids = cids + Strings.repeat(",", 4 - commaCount);
//            }
//        }

        int catIdx = line.indexOf("cat=", MIN_CATEGORY_POS);
        String cids = ",,,,";
        if (catIdx != -1) {
            int endCatIdx = line.indexOf(";", catIdx + 4);
            cids = line.substring(catIdx + 4, endCatIdx);
            if (cids.charAt(1) != '_') {
                cids = cids.substring(cids.lastIndexOf(',') + 1);
            }

            String[] cparts = cids.split("_");

            StringBuilder sb = new StringBuilder(49);
            sb.append(cparts[0]);
            for (int i = 1; i < cparts.length; i++) {
                sb.append(",").append(cparts[0]).append(cparts[i]);
            }
            for (int i = cparts.length; i <= 4; i++) {
                sb.append(",");
            }
            cids = sb.toString();
        }

        return cids;
    }


    public static String extractEruid(String line) {
        int eruidIdx = line.indexOf("erUid=", MIN_ERUID_POS) + 6;
        int endEruidIdx = line.indexOf(";", eruidIdx);

        if (endEruidIdx < 0) {
            endEruidIdx = line.indexOf(" ", eruidIdx);
        }

        return (line.substring(eruidIdx + 9, eruidIdx + 20) + line.charAt(endEruidIdx - 1)).replace("-", "");
    }

    public static String extractPlist(String line) {
        int plistIdx = line.indexOf("plist=", MIN_PLIST_POS) + 6;
        int endPlistIdx = line.indexOf(";", plistIdx);

        return line.substring(plistIdx, endPlistIdx);
    }

    public static char extractEturec(String line) {
        int plistIdx = line.indexOf("eturec=", MIN_ETUREC_POS);
        if (plistIdx < 0) {
            return 'N';
        }
        return 'Y';
    }

    public static String extractUid(String line) {
        int uidIdx = line.indexOf("uid=", MIN_UID_POS) + 4;
        int endUidIdx = line.indexOf(";", uidIdx);

        return line.substring(uidIdx, endUidIdx);
    }

    public static short extractHour(String line) {
        int timeIdx = line.indexOf(":", MIN_HOUR_COLON_POS) + 1;
        int hour = timeIdx + 2;
        return Short.parseShort(line.substring(timeIdx, hour));
    }

    public static String extractWeekOfDay(String line) {
        int timeIdx = line.indexOf(":", MIN_HOUR_COLON_POS) - 11;
        int day = timeIdx + 6;

        return WeekUtil.weeks.get(line.substring(timeIdx, day));
    }

    public static String extractIp(String line) {
        int ipIdx = line.indexOf(" ", MIN_IP_SPACE_POS);
        return line.substring(0, ipIdx);
    }

    public static char extractDevice(String line) {
        int uaIdx = line.indexOf("\" \"", MIN_USER_AGENT_SPACE_POS);
        String ua = line.substring(uaIdx + 3);

        char device = 'D'; // other
        for(int i =0; i < DEVICIES_STR.length; i++) {
            if(ua.contains(DEVICIES_STR[i])) {
                device = DEVICIES[i];
                break;
            }
        }

        return device;
    }

    public static String extractPid(String line) {
        int pidIdx = line.indexOf("pid=", MIN_PID_POS);
        int endPidIdx = line.indexOf(";", pidIdx + 4);
        return line.substring(pidIdx + 4, endPidIdx);
    }
}
