package com.client.socket.lib;

import com.client.socket.utils.DateUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class WebSocketBase {

    private Logger log = LogManager.getLogger(WebSocketBase.class);
    private WebSocketService service = null;
    private Timer timerTask = null;
    private MonitorTask monitor = null;
    private EventLoopGroup group = null;
    private Bootstrap bootstrap = null;
    private Channel channel = null;
    private String url = null;
    private ChannelFuture future = null;
    private boolean isAlive = false;
    private Set<String> subscribeChannel = new HashSet<>();

    public WebSocketBase(String url, WebSocketService service) {
        this.url = url;
        this.service = service;
    }

    public void start() {
        if (url == null) {
            log.info("WebSocketClient start error  url can not be null");
            return;
        }
        if (service == null) {
            log.info("WebSocketClient start error  WebSocketService can not be null");
            return;
        }
        monitor = new MonitorTask(this);
        this.connect();
        timerTask = new Timer();
        timerTask.schedule(monitor, 1000, 5000);
    }

    public void setStatus(boolean flag) {
        this.isAlive = flag;
    }

    public void addChannel(String channel) {
        if (channel == null) {
            return;
        }
//        msgs.add("{\"sub\":\"market.eosusdt.kline.1min\",\"id\":\"id1\"}");
//        msgs.add("{\"sub\":\"market.btcusdt.kline.1min\",\"id\":\"id1\"}");
        String msg1 = "{\"req\":\"market.symbols\"}";
        String dataMsg = "{\"sub\":\"market.btcusdt.detail\",\"symbol\":\"btcusdt\"}";
        this.sendMessage(msg1);
        this.sendMessage(dataMsg);
        subscribeChannel.add(channel);
    }

    public void removeChannel(String channel) {
        if (channel == null) {
            return;
        }
//        String dataMsg = "{'event':'removeChannel','channel':'" + channel + "'}";
//        this.sendMessage(dataMsg);
        subscribeChannel.remove(channel);
    }

    public void closeChannel() {
        try {
            this.channel.close();
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private void connect() {
        try {
            final URI uri = new URI(url);
            String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
            final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();

            final int port;

            if (uri.getPort() == -1) {
                if ("ws".equalsIgnoreCase(scheme)) {
                    port = 80;
                } else if ("wss".equalsIgnoreCase(scheme)) {
                    port = 443;
                } else {
                    port = -1;
                }
            } else {
                port = uri.getPort();
            }

            if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                log.info("Only WS(S) is supported.");
                return;
            }

            final boolean ssl = "wss".equalsIgnoreCase(scheme);
            final SslContext sslCtx;
            if (ssl) {
                sslCtx = SslContext.newClientContext();
            } else {
                sslCtx = null;
            }
            group = new NioEventLoopGroup(1);
            bootstrap = new Bootstrap();
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
                            WebSocketVersion.V13, null, false,
                            new DefaultHttpHeaders(), Integer.MAX_VALUE), service, monitor);
            bootstrap.group(group).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    handler);
                        }
                    });
            future = bootstrap.connect(host, port);
            future.await(5, TimeUnit.SECONDS);
            log.info("Start to wait websocket handShake future....");
            handler.handshakeFuture().await(5, TimeUnit.SECONDS);
            if (future.isSuccess() && handler.handshakeFuture().isSuccess()) {
                log.info("Connect to websocket " + url + " success");
                channel = future.channel();
                this.setStatus(true);
            } else {
                group.shutdownGracefully();
                this.setStatus(false);
                throw new RuntimeException("Failed to connect websocket cause connect time out " + url);
            }
        } catch (Exception e) {
            log.info("WebSocketClient start error ", e);
            group.shutdownGracefully();
            this.setStatus(false);
        }
    }

    private void sendMessage(String message) {
        if (!isAlive) {
            log.info("WebSocket is not Alive addChannel error");
        }else {
            channel.writeAndFlush(new TextWebSocketFrame(message));
        }
    }

    public void sentPing() {
        String dataMsg = "ping";
        this.sendMessage(dataMsg);
    }

    public void reConnect() {
        try {
            this.group.shutdownGracefully();
            this.group = null;
            this.connect();
            if (future.isSuccess()) {
                this.setStatus(true);
//                this.sentPing();
                log.info("Reconnect future success, time:" + DateUtil.convertToString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
                Iterator<String> it = subscribeChannel.iterator();
                while (it.hasNext()) {
                    String channel = it.next();
                    log.info("Start add channel:" + channel + " time:" + DateUtil.convertToString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
                    this.addChannel(channel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Timer getTimeTask() {
        return this.timerTask;
    }
}

class MonitorTask extends TimerTask {

    private Logger log = LogManager.getLogger(WebSocketBase.class);
    private long startTime = System.currentTimeMillis();
    private int checkTime = 10000;
    private WebSocketBase client = null;

    public void updateTime() {
        //log.info("startTime is update");
        startTime = System.currentTimeMillis();
    }

    public void updateStatus(boolean status){
        this.client.setStatus(status);
    }

    public MonitorTask(WebSocketBase client) {
        this.client = client;
        //log.info("TimerTask is starting.... ");
    }

    public void run() {
        if (System.currentTimeMillis() - startTime > checkTime) {
            log.info("start reconnect time:" + DateUtil.convertToString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
            client.reConnect();
            log.info("end reconnect time:" + DateUtil.convertToString(new Date(), DateUtil.YYYY_MM_DD_HH_MM_SS));
        }
//        client.sentPing();
        // log.info("Moniter ping data sent.... ");
    }

}  
