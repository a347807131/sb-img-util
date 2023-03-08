package com.example.sbimgutil.rest.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的返回的消息类
 *
 * @author Civin
 */
@Data
public class R<T> implements Serializable, IResult {

    public static int CODE_OK = 200;
    public static int CODE_NOK = 500;
    public static String MSG_OK = "process successed";
    public static String MSG_NOK = "process failed";
    //返回的数据
    T data;
    //状态码 200-成功 500-失败
    private int code;
    //提示信息
    private String msg;

    R() {
    }

    public static <T> R<T> of(String msg, int code) {
        var dto = new R<T>();
        dto.msg = msg;
        dto.code = code;
        return dto;
    }

    public static <T> R<T> of(String msg, int code, T data) {
        var dto = new R<T>();
        dto.data = data;
        dto.msg = msg;
        dto.code = code;
        return dto;
    }

    public static <T> R<T> ok() {
        return of(MSG_OK, CODE_OK);
    }

    public static <T> R<T> ok(T data) {
        return of(MSG_OK, CODE_OK, data);
    }

    public static <T> R<T> nok() {
        return of(MSG_NOK, CODE_NOK);
    }

    public static <T> R<T> nok(IStatusEnum statusEnum) {
        return of(statusEnum.getMessage(), statusEnum.getCode());
    }

    public static <T> R<T> nok(T data) {
        return of(MSG_NOK, CODE_NOK, data);
    }
}



