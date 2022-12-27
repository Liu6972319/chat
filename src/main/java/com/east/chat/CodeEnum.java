package com.east.chat;

public enum CodeEnum {
    SUCCESS(200,"success"),
    ERROR(500,"error");

    private int code;
    private String msg;
    CodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
