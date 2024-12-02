package com.digitalcopyright.utils;

import org.springframework.stereotype.Component;

/**
 * @author Sakura
 */
public class RedisTransKey {

    public static final String RedisNameSpace="user";
    public static final String RedisTokenName="token";
    public static final String RedisLoginName="login";
    public static final String RedisEmailCodeName="emailCode";

    public static String setEmailKey(String key){
        return RedisNameSpace+":"+RedisEmailCodeName+":"+key;
    }
    public static String setRootKey(String key){
        return RedisNameSpace+":"+key+":";
    }
    public static String setTokenKey(String key){
        return RedisNameSpace+':'+RedisTokenName+":"+key;
    }
    public static String setLoginKey(String key){
        return RedisNameSpace+':'+RedisLoginName+":"+key;
    }

    public static String getEmailKey(String key){return setEmailKey(key);}
    public static String getRootKey(String key){return setRootKey(key);}
    public static String getTokenKey(String key){return setTokenKey(key);}
    public static String getLoginKey(String key){return setLoginKey(key);}

}

