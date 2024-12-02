package com.digitalcopyright.utils;

import com.digitalcopyright.model.DO.UsersDO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtil {

    private static String secret;
    private static Long expiration;
    private static Map<String, Object> header;
    static {
        secret="a8c52b77f877846bd06f6c6a4e184c31d54fbff1e674468f7754878091e5bd30c3614cbb292a9dc28d139889b3f43af842833121372def3fc8c4563b9da82c91";
        expiration = 7*24*60*60*1000L;
        header=new HashMap<>();
        header.put("typ", "jwt");


    }


    /**
     * 生成token令牌
     * @return 令token牌
     */
    public static String generateToken(UsersDO user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("userid",user.getId());
        claims.put("created", new Date());
        return generateToken(claims);
    }

    /**
     * @param token 令牌
     * @return 用户名
     */
    public static String GetUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get("username");
        } catch (Exception e) {
            username = null;
        }
        return username;
    }
    public static String GetUserIDFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get("userid");
        } catch (Exception e) {
            username = null;
        }
        return username;
    }
    /**
     * 判断令牌是否过期
     * @param token 令牌
     * @return 是否过期
     */
    public static Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新令牌
     *
     * @param token 原令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            Claims claims = getClaimsFromToken(token);
            claims.put("created", new Date());
            refreshedToken = generateToken(claims);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 验证令牌
     * @return 是否有效
     */
    public static Boolean validateToken(String token, UsersDO user) {
        String username = GetUserNameFromToken(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }


    /**
     * 从claims生成令牌,如果看不懂就看谁调用它
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private static String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder()
                .setHeader(header)
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 从令牌中获取数据声明,如果看不懂就看谁调用它
     *
     * @param token 令牌
     * @return 数据声明
     */
    public static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

}
