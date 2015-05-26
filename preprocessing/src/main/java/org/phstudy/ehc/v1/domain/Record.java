package org.phstudy.ehc.v1.domain;

import eu.bitwalker.useragentutils.UserAgent;

import java.util.Date;
import java.util.Map;


/**
 * Created by study on 5/16/15.
 */
public class Record {
    private UserAgent ua;
    private String ip;
    private Date ts;
    private Map<String, Object> data;
    private int code;
    private int bytes;
    private String act;

    @Override
    public String toString() {
        return "Record{" +
                "ua=" + getUa() +
                ", ip='" + getIp() + '\'' +
                ", ts=" + getTs() +
                ", data=" + getData() +
                ", code=" + getCode() +
                ", bytes=" + getBytes() +
                ", referer='" + getReferer() + '\'' +
                '}';
    }

    private String referer;

    public Record(UserAgent ua, String ip, Date ts, Map<String, Object> data, int code, int bytes, String referer) {
        this.setUa(ua);
        this.setIp(ip);
        this.setTs(ts);
        this.setData(data);
        this.setCode(code);
        this.setBytes(bytes);
        this.setReferer(referer);
    }

    public UserAgent getUa() {
        return ua;
    }

    public void setUa(UserAgent ua) {
        this.ua = ua;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
        setAct((String) data.get("act"));
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }
}
