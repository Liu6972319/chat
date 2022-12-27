package com.east.chat;

import cn.hutool.core.util.StrUtil;
import io.netty.util.internal.StringUtil;
import lombok.Data;

@Data
public class AjaxResult {
    private int code;
    private String msg;

    private Object data;

    public AjaxResult(CodeEnum codeEnum) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
    }
    public AjaxResult(CodeEnum codeEnum,Object data) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
        this.data = data;
    }

    public AjaxResult(CodeEnum codeEnum, String error) {
        this.code = codeEnum.getCode();
        if (StrUtil.isBlankIfStr(error)) {
            this.msg = codeEnum.getMsg();
        } else {
            this.msg = error;
        }
    }

    public AjaxResult(int code, String error) {
        this.code = code;
        this.msg = error;
    }

    public static AjaxResult success() {
        return new AjaxResult(CodeEnum.SUCCESS);
    }
    public static AjaxResult success(Object data) {
        return new AjaxResult(CodeEnum.SUCCESS,data);
    }

    public static AjaxResult error(int code, String error) {
        return new AjaxResult(code, error);
    }

    public static AjaxResult error(String error) {
        return new AjaxResult(CodeEnum.ERROR, error);
    }

    public static AjaxResult error() {
        return new AjaxResult(CodeEnum.ERROR);
    }
}
