package ord.phstudy.ehc;

public class Record {
    public boolean isTrain = false;
    public String uid;
    public String eruid;
    public String cid = ",,,,";
    public String pid;
    public short hour;
    public String ip;
    public String ua;
    public char etured = 'N';
    public String weekOfDay;
    public char buy;
    public short viewnum = 1;
    public int price;
    public short num;

    @Override
    public String toString() {
        String comma = ",";
        StringBuilder sb = new StringBuilder(95);
        if (isTrain) {
            sb.append(weekOfDay).append(comma)
                    .append(hour).append(comma)
                    .append(eruid).append(comma)
                    .append(cid).append(comma)
                    .append(etured).append(comma)
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
                    .append(etured).append(comma)
                    .append(pid).append(comma)
                    .append(viewnum).append(comma)
                    //.append(ip).append(comma)
                    //.append(ua).append(comma)
                    .append(uid).append(comma)
                    .append(price);
        }

        return sb.toString();
    }
}