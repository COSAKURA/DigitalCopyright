package com.digitalcopyright.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * @author Sakura
 */
public class EncryptionUtil {

    /**
     * 对文件内容进行 Base64 编码
     *
     * @param filePath 文件路径
     * @return Base64 编码后的字符串
     * @throws Exception 如果读取文件失败则抛出异常
     */
    public static String encodeBase64(Path filePath) throws Exception {
        byte[] fileBytes = Files.readAllBytes(filePath);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    /**
     * 计算字符串的 SHA-256 哈希值
     *
     * @param data 输入字符串
     * @return SHA-256 哈希值
     * @throws Exception 如果计算失败则抛出异常
     */
    public static String calculateSHA256(String data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data.getBytes("UTF-8"));

        // 转换为十六进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
