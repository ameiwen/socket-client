package com.client.socket.lib;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;


public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;
    private MonitorTask monitor;
    private WebSocketService service;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, WebSocketService service, MonitorTask monitor) {
        this.handShaker = handshaker;
        this.service = service;
        this.monitor = monitor;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)  throws Exception {
        handShaker.handshake(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }


    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        monitor.updateTime();
        //握手
        if (!handShaker.isHandshakeComplete()) {
            handShaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            System.out.println("TextWebSocketFrame receive:" + textFrame.text());
            service.onReceive(textFrame.text());
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            String rs = uncompress(binaryFrame.content());
            if(rs.contains("ping")) {
                //log.info("send:" + str.replace("ping", "pong"));
                ch.writeAndFlush(new TextWebSocketFrame(rs.replace("ping", "pong")));
            }
            service.onReceive(rs);
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            monitor.updateStatus(false);
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    /**
     * 解码Binary
     * @return
     * @throws IOException
     * @throws DataFormatException
     */
    public String uncompress(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        GZIPInputStream gzipInputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            //使用org.apache.commons.io.IOUtils 简化流的操作
            IOUtils.copy(gzipInputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放流资源
            IOUtils.closeQuietly(gzipInputStream);
            IOUtils.closeQuietly(byteArrayInputStream);
            IOUtils.closeQuietly(byteArrayOutputStream);
        }
        return null;
    }

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Object msg) {

	}

}
