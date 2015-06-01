package org.phstudy.ehc.domain;

public class Record {
    public static final String DEFAULT_CID = ",,,,";
    public static final char DEFAULT_ETUREC = 'N';
    public static final char DEFAULT_BUY = 'N';

    public boolean isTrain = false;
    public String uid;
    public String eruid;
    public String cid = DEFAULT_CID;
    public String pid;
    public short day;
    public short hour;
    public String ip;
    public char device;
    // A:iPhone
    // B:iPad
    // C:Android
    // D:Other

    public char eturec = DEFAULT_ETUREC;
    public String weekOfDay;
    public char buy = DEFAULT_BUY;
    public int viewnum = 1;
    public int price = 0;
    public short num = 0;

    @Override
    public String toString() {
        String comma = ",";
        StringBuilder sb = new StringBuilder(95);
        if (isTrain) {
            sb.append(weekOfDay).append(comma)
                    .append(day).append(comma)
                    .append(hour).append(comma)
                    .append(eruid).append(comma)
                    .append(cid).append(comma)
                    .append(eturec).append(comma)
                    .append(pidToUpid(pid)).append(comma)
                    .append(pid).append(comma)
                    .append(viewnum).append(comma)
                    //.append(ip).append(comma)
                    .append(charToDevice(device)).append(comma)
                    //.append(uid).append(comma)
                    .append("".equals(uid) ? 'N' : 'Y').append(comma) // isLogin
                    .append(price).append(comma)
                    .append(buy).append(comma)
                    .append(num);
        } else {
            sb.append(weekOfDay).append(comma)
                    .append(day).append(comma)
                    .append(hour).append(comma)
                    .append(eruid).append(comma)
                    .append(cid).append(comma)
                    .append(eturec).append(comma)
                    .append(pidToUpid(pid)).append(comma)
                    .append(pid).append(comma)
                    .append(viewnum).append(comma)
                    //.append(ip).append(comma)
                    .append(charToDevice(device)).append(comma)
                    //.append(uid).append(comma)
                    .append("".equals(uid) ? 'N' : 'Y').append(comma)
                    .append(price);
        }

        return sb.toString();
    }

    public static String getHeader(boolean isTrain) {
        String comma = ",";
        StringBuilder sb = new StringBuilder(95);
        if (isTrain) {
            sb.append("weekOfDay").append(comma)
                    .append("day").append(comma)
                    .append("hour").append(comma)
                    .append("eruid").append(comma)
                    .append("class1").append(comma)
                    .append("class2").append(comma)
                    .append("class3").append(comma)
                    .append("class4").append(comma)
                    .append("class5").append(comma)
                    .append("eturec").append(comma)
                    .append("upid").append(comma)
                    .append("pid").append(comma)
                    .append("viewnum").append(comma)
                    //.append("ip").append(comma)
                    .append("device").append(comma)
                    //.append("uid").append(comma)
                    .append("login").append(comma)
                    .append("price").append(comma)
                    .append("buy").append(comma)
                    .append("num");
        } else {
            sb.append("weekOfDay").append(comma)
                    .append("day").append(comma)
                    .append("hour").append(comma)
                    .append("eruid").append(comma)
                    .append("class1").append(comma)
                    .append("class2").append(comma)
                    .append("class3").append(comma)
                    .append("class4").append(comma)
                    .append("class5").append(comma)
                    .append("eturec").append(comma)
                    .append("upid").append(comma)
                    .append("pid").append(comma)
                    .append("viewnum").append(comma)
                    //.append("ip").append(comma)
                    .append("device").append(comma)
                    //.append("uid").append(comma)
                    .append("login").append(comma)
                    .append("price");
        }

        return sb.toString();
    }

    public static String charToDevice(char c) {
        switch (c) {
            case 'A':
                return "iPhone";
            case 'B':
                return "iPad";
            case 'C':
                return "Android";
            default:
                return "Other";
        }
    }

    public static String charToBooleanString(char c) {
        switch (c) {
            case 'Y':
                return "TRUE";
            default:
                return "FALSE";
        }
    }

    public static String pidToUpid(String pid) {
        return pid.substring(0, 9);
    }
}