package ord.phstudy.ehc;

public class Record {
    public String uid;
    public String eruid;
    public String cid;
    public String pid;
    public short hour;
    public String ip;
    public String ua;
    public char etured;
    public String weekOfDay;
    public char buy;
    public short viewnum;
    public short price;
    public short num;

    @Override
    public String toString() {
        String comma = ",";
        StringBuilder sb = new StringBuilder(); // TODO: size
        sb.append(uid).append(comma)
                .append(eruid).append(comma)
                .append(cid).append(comma)
                .append(pid).append(comma)
                .append(hour).append(comma)
                .append(ip).append(comma)
                //.append(ua).append(comma)
                .append(etured).append(comma)
                .append(weekOfDay).append(comma)
                .append(buy).append(comma)
                .append(viewnum).append(comma)
                .append(price).append(comma)
                .append(num);

        return sb.toString();
    }
}