package com.digitalcopyright.common;

/**
 * @author Sakura
 */

public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    HAS_USERNAME(10002, "已存在该用户"),
    OVER_REQUESTS(10003, "访问频次过多"),
    OVER_TIME(10004, "操作超时"),
    BAD_DOING(10005, "疑似恶意操作"),
    BAD_EMAILCODE_VERIFY(10007, "邮箱验证码错误"),
    REPARATION_GO(10008, "请重新操作"),
    NO_SUCHUSER(10009, "该用户不存在"),
    BAD_PUTDATA(10010, "信息提交错误，请重新检查"),
    SUCCESSFUL(200, "successful");

    private int code;
    private String msg;

    BizCodeEnum(int code, String msg) {
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
