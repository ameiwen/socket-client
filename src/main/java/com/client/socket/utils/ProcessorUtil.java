package com.client.socket.utils;


public class ProcessorUtil {

    private static String commodityType = PropertyReader.get("processor.commodityType", "processor.properties");

    private static final String okcoinCnWebSocketUrl = PropertyReader.get("websocket.url", "processor.properties");
    private static final String okcoinCnRestUrl = PropertyReader.get("rest.url", "processor.properties");



    public static String getCommodityType() {
        return commodityType;
    }


    public static String getOkcoinCnWebSocketUrl() {
        return okcoinCnWebSocketUrl;
    }

}
