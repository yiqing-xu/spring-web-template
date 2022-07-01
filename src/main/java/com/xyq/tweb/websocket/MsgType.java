package com.xyq.tweb.websocket;

public enum MsgType {

    HEART_BEAT(1, -1, "心跳检测"),
    CHAT_SEND(2, -2, "发送消息"),
    CHAT_RECEIVE(3, -3, "接收消息");

    private Integer success;
    private Integer fail;
    private String action;

    MsgType() {
    }

    MsgType(Integer success, Integer fail, String action) {
        this.success = success;
        this.fail = fail;
        this.action = action;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getFail() {
        return fail;
    }

    public void setFail(Integer fail) {
        this.fail = fail;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
