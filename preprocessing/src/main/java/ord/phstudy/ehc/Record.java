package ord.phstudy.ehc;

public class Record {
    public static final String DEFAULT_CID = ",,,,";
    public static final char DEFAULT_ETUREC = 'N';
    public static final char DEFAULT_BUY = 'N';



    public boolean isTrain = false;
    public String uid;
    public String eruid;
    public String cid = DEFAULT_CID;
    public String pid;
    public short hour;
    public String ip;
    public String ua;
    public char eturec = DEFAULT_ETUREC;
    public String weekOfDay;
    public char buy = DEFAULT_BUY;
    public short viewnum = 1;
    public int price = 0;
    public short num = 0;

    @Override
    public String toString() {
        String comma = ",";
        StringBuilder sb = new StringBuilder(95);
        if (isTrain) {
            sb.append(weekOfDay).append(comma)
                    .append(hour).append(comma)
                    .append(eruid).append(comma)
                    .append(cid).append(comma)
                    .append(eturec).append(comma)
                    .append(pid).append(comma)
                    .append(viewnum).append(comma)
                    //.append(ip).append(comma)
                    //.append(ua).append(comma)
                    .append(uid).append(comma)
                    .append(price).append(comma)
                    .append(buy).append(comma)
                    .append(num);
        } else {
            sb.append(weekOfDay).append(comma)
                    .append(hour).append(comma)
                    .append(eruid).append(comma)
                    .append(cid).append(comma)
                    .append(eturec).append(comma)
                    .append(pid).append(comma)
                    .append(viewnum).append(comma)
                    //.append(ip).append(comma)
                    //.append(ua).append(comma)
                    .append(uid).append(comma)
                    .append(price);
        }

        return sb.toString();
    }

    public String getHeader() {
        String comma = ",";
        StringBuilder sb = new StringBuilder(95);
        if (isTrain) {
            sb.append("weekOfDay").append(comma)
                    .append("hour").append(comma)
                    .append("eruid").append(comma)
                    .append("cid").append(comma)
                    .append("eturec").append(comma)
                    .append("pid").append(comma)
                    .append("viewnum").append(comma)
                    //.append("ip").append(comma)
                    //.append("ua").append(comma)
                    .append("uid").append(comma)
                    .append("price").append(comma)
                    .append("buy").append(comma)
                    .append("num");
        } else {
            sb.append("weekOfDay").append(comma)
                    .append("hour").append(comma)
                    .append("eruid").append(comma)
                    .append("cid").append(comma)
                    .append("eturec").append(comma)
                    .append("pid").append(comma)
                    .append("viewnum").append(comma)
                    //.append("ip").append(comma)
                    //.append("ua").append(comma)
                    .append("uid").append(comma)
                    .append("price");
        }

        return sb.toString();
    }
}