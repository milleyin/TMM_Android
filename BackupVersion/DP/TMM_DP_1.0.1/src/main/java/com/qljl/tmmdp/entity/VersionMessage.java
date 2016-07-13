package com.qljl.tmmdp.entity;

/**
 * Created by luow on 2016/5/19.
 */
public class VersionMessage {
    private int version;//版本
    private String url;//下载地址

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
