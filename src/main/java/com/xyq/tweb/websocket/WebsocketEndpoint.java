package com.xyq.tweb.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2022/6/22
 */
@Component
@ServerEndpoint("/ws/{sourceId}")
public class WebsocketEndpoint {

    private static Channel channel;
    private static final Logger logger = LogManager.getLogger();

    public WebsocketEndpoint() {
    }

    @Autowired
    public WebsocketEndpoint(Channel channel) {
        WebsocketEndpoint.channel = channel;
    }

    @OnOpen
    public void onOpen(@PathParam("sourceId") String sourceId, Session session) {
        channel.add(sourceId, session);
        logger.info("{}建立websocket连接", sourceId);
    }

    @OnMessage
    public void onMessage(@PathParam("sourceId") String sourceId, String msg, Session session) {
        channel.handleMessage(sourceId, msg);
    }

    @OnClose
    public void onClose(@PathParam("sourceId") String sourceId) {
        channel.remove(sourceId);
        logger.warn("{}断开websocket连接", sourceId);
    }

    @OnError
    public void onError(@PathParam("sourceId") String sourceId, Throwable throwable) {
        channel.remove(sourceId);
        throwable.printStackTrace();
    }

}
