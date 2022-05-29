package com.xyq.tweb.exception;

import com.xyq.tweb.domain.web.Msg;

public class UnauthorizedException extends RuntimeException {

    private Integer code;

    private String msg;

    public UnauthorizedException() {
    }

    public UnauthorizedException(String msg) {
        this.code = Msg.UNAUTHORIZED.getCode();
        this.msg = msg;
    }

    public UnauthorizedException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public UnauthorizedException setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public UnauthorizedException setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}
