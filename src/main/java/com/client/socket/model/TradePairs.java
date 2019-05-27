package com.client.socket.model;

import java.io.Serializable;

public class TradePairs implements Serializable {
    private Integer areaId ;
    private String areaName ;
    private String pairs ;
    private String currPairs ;
    private Integer scale1;
    private Integer scale2;

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getPairs() {
        return pairs;
    }

    public void setPairs(String pairs) {
        this.pairs = pairs;
    }

    public String getCurrPairs() {
        return currPairs;
    }

    public void setCurrPairs(String currPairs) {
        this.currPairs = currPairs;
    }

    public Integer getScale1() {
        return scale1;
    }

    public void setScale1(Integer scale1) {
        this.scale1 = scale1;
    }

    public Integer getScale2() {
        return scale2;
    }

    public void setScale2(Integer scale2) {
        this.scale2 = scale2;
    }
}
