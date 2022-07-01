package com.xyq.tweb.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xyq.tweb.domain.web.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2022/6/22
 */
@Component
public class Channel {

    private ObjectMapper mapper;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private static final Logger logger = LogManager.getLogger();

    public Channel() {
    }

    @Autowired
    public Channel(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void add(String sourceId, Session session) {
        sessions.put(sourceId, session);
    }

    public void remove(String sourceId) {
        sessions.remove(sourceId);
    }

    public void sendMsg(String sourceId, String msg) {
        Session session = sessions.get(sourceId);
        if (session != null) {
            session.getAsyncRemote().sendText(msg);
        }
    }

    public void sendMsg(String sourceId, Object obj) {
        String text = toJsonString(obj);
        sendMsg(sourceId, text);
    }

    public void handleMessage(String sourceId, String text) {
        logger.info("从source-{}-接收到数据-{}", sourceId, text);
        Message message = parseObject(text, Message.class);
        message.setSourceId(sourceId);
        MsgType type = message.getType();
        if (type.equals(MsgType.HEART_BEAT)) {
            handleHeartBeat(message);
        }
        if (type.equals(MsgType.CHAT_SEND)) {
            handleChat(message);
        }
    }

    public void handleHeartBeat(Message message) {
        Result<Void> result = new Result<>();
        result.setCode(MsgType.HEART_BEAT.getSuccess());
        result.setMsg(MsgType.HEART_BEAT.getAction());
        sendMsg(message.getSourceId(), result);
    }

    public void handleChat(Message message) {
        Result<Void> source = new Result<>();
        source.setCode(MsgType.CHAT_SEND.getSuccess());
        source.setMsg(MsgType.CHAT_SEND.getAction());
        sendMsg(message.getSourceId(), source);

        Result<String> target = new Result<>();
        target.setCode(MsgType.CHAT_RECEIVE.getSuccess());
        target.setMsg(MsgType.CHAT_RECEIVE.getAction());
        target.setData(message.getText());
        sendMsg(message.getTargetId(), target);
    }

    private String toJsonString(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T> T parseObject(String text, Class<T> t) {
        T obj = null;
        try {
            obj = mapper.readValue(text, t);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            try {
                obj = t.newInstance();
            } catch (InstantiationException | IllegalAccessException instantiationException) {
                instantiationException.printStackTrace();
            }
        }
        return obj;
    }

}
