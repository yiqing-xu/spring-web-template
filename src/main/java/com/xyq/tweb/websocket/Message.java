package com.xyq.tweb.websocket;

/**
 * <p>
 *
 * </p>
 *
 * @author xuyiqing
 * @since 2022/6/22
 */
public class Message {

    private String sourceId;

    private String targetId;

    private String text;

    private MsgType type;

    public String getSourceId() {
        return sourceId;
    }

    public Message setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public String getTargetId() {
        return targetId;
    }

    public Message setTargetId(String targetId) {
        this.targetId = targetId;
        return this;
    }

    public String getText() {
        return text;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }
}
