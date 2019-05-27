package com.client.socket;

import java.util.Calendar;

import com.client.socket.client.KlineMessageReceiver;
import com.client.socket.lib.WebSocketClient;
import com.client.socket.utils.ProcessorUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OkCoinPublishTask extends ProcessTask {

    private WebSocketClient socketClient;
    private static final String channel = "market.btcusdt.detail";
    private static final Logger logger = LogManager.getLogger(OkCoinPublishTask.class);


    public OkCoinPublishTask() {
        this.socketClient = new WebSocketClient(ProcessorUtil.getOkcoinCnWebSocketUrl(), new KlineMessageReceiver());
        socketClient.start();
        socketClient.addChannel(channel);
    }

    @Override
    public void process() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.MINUTE, -61);

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }

        try {
            // 每隔一分钟抓一次
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void init() throws Exception {
        logger.info("OkCoinPublishTask start running.");
    }

    @Override
    public void end() throws Exception {
        logger.info("OkCoinPublishTask end.");
    }

    public void shutdown() {
        super.shutdown();
        socketClient.getTimeTask().cancel();
        socketClient.removeChannel(channel);
        socketClient.closeChannel();
    }

    public void down() {
        super.down();
        socketClient.getTimeTask().cancel();
        socketClient.removeChannel(channel);
        socketClient.closeChannel();
    }
}
