package com.xyq.tweb.domain.web;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer code;

    private String msg;

    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result(T data) {
        this.code = Msg.SUCCESS.getCode();
        this.msg = Msg.SUCCESS.getMsg();
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>().setCode(Msg.SUCCESS.getCode()).setMsg(Msg.SUCCESS.getMsg()).setData(data);
    }

    public static Result<Void> badRequest(String msg) {
        return new Result<Void>().setCode(Msg.BAD_REQUEST.getCode()).setMsg(msg);
    }

    public static Result<Void> error(String msg) {
        return new Result<Void>().setCode(Msg.SERVER_ERROR.getCode()).setMsg(msg);
    }

    public Integer getCode() {
        return code;
    }

    public Result<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Result<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public Result<T> setData(T data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
