package com.xyq.tweb.exception;

import com.xyq.tweb.domain.web.Msg;

public class BadRequestException extends RuntimeException {

    private Integer code;

    private String msg;

    public BadRequestException() {
    }

    public BadRequestException(String msg) {
        this.code = Msg.BAD_REQUEST.getCode();
        this.msg = msg;
    }

    public BadRequestException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public BadRequestException setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BadRequestException setMsg(String msg) {
        this.msg = msg;
        return this;
    }
}
