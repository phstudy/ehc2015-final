package org.phstudy.ehc.v1.domain;

/**
 * Created by study on 5/16/15.
 */
public class ProductRecord {
    private String pid;
    private int price;
    private int num;

    public ProductRecord(String pid, int num, int price) {
        this.pid = pid;
        this.price = price;
        this.num = num;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "{" +
                "pid=\"" + pid + '\"' +
                ", price=" + price +
                ", num=" + num +
                '}';
    }
}
