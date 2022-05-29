package com.xyq.tweb.domain.web;

public enum Msg {

    SUCCESS(200, "返回成功"),

    BAD_REQUEST(400, "请求参数错误"),

    UNAUTHORIZED(401, "身份认证失败"),

    FORBIDDEN(403, "无权限访问"),

    NOT_FOUND(404, "请求资源不存在"),

    SERVER_ERROR(500, "服务器出小差了, 请稍候再试^_^");

    private Integer code;

    private String msg;

    Msg(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public Msg setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Msg setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public String toString() {
        return "Msg{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
