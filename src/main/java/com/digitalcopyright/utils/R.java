package com.digitalcopyright.utils;

import org.springframework.stereotype.Component;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {
    @Serial
    private static final long serialVersionUID = 1L;

    // 构造函数初始化默认值
    public R() {
        put("code", 0);
        put("msg", "success");
        put("timestamp", System.currentTimeMillis());
    }

    // 错误响应方法
    public static R error() {
        return error(500, "未知异常，请联系管理员");
    }

    public static R error(String msg) {
        return error(500, msg);
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    // 成功响应方法
    public static R ok(String msg) {
        R r = new R();
        r.put("msg", msg);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    // 重写put方法，支持链式调用
    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
