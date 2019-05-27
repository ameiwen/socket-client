package com.client.socket.client;


import com.client.socket.lib.WebSocketService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class KlineMessageReceiver implements WebSocketService {

    private static final Logger logger = LogManager.getLogger(KlineMessageReceiver.class);

    @Override
    public void onReceive(String msg) {
        try {
            logger.info("rece:" + msg);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
