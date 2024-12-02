package com.digitalcopyright.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

public class KeystoreUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int AES_KEY_SIZE = 256; // AES 密钥大小（256 位）
    private static final int GCM_IV_LENGTH = 12; // GCM 初始向量长度（12 字节）

    /**
     * 生成 Keystore 文件，保存加密的私钥
     *
     * @param keyPair     区块链密钥对
     * @param password    用于生成加密密钥的密码
     * @param keystorePath Keystore 文件保存路径
     * @throws Exception 发生异常时抛出
     */
    public static void generateKeystore(CryptoKeyPair keyPair, String password, String keystorePath) throws Exception {
        // 将私钥转换为字节数组
        byte[] privateKeyData = keyPair.getHexPrivateKey().getBytes(StandardCharsets.UTF_8);

        // 生成 AES 密钥
        SecretKey secretKey = generateSecretKey(password);

        // 生成随机 IV（初始向量）
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // 使用 AES/GCM/NoPadding 加密
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        byte[] encryptedData = cipher.doFinal(privateKeyData);

        // 保存加密数据（包含 IV 和加密私钥）
        String encodedIV = Base64.getEncoder().encodeToString(iv);
        String encodedEncryptedData = Base64.getEncoder().encodeToString(encryptedData);
        String keystoreContent = encodedIV + ":" + encodedEncryptedData;

        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            fos.write(keystoreContent.getBytes(StandardCharsets.UTF_8));
        }

        System.out.println("Keystore 文件已生成: " + keystorePath);
    }

    /**
     * 从 Keystore 文件中加载私钥
     *
     * @param keystorePath Keystore 文件路径
     * @param password     用于生成解密密钥的密码
     * @return 解密后的私钥字符串
     * @throws Exception 发生异常时抛出
     */
    public static String loadPrivateKeyFromKeystore(String keystorePath, String password) throws Exception {
        // 读取 Keystore 文件
        byte[] fileContent = Files.readAllBytes(new File(keystorePath).toPath());
        String keystoreContent = new String(fileContent, StandardCharsets.UTF_8);

        // 分割 IV 和加密数据
        String[] parts = keystoreContent.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Keystore 文件格式无效");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

        // 使用密码生成 AES 密钥
        SecretKey secretKey = generateSecretKey(password);

        // 使用 AES/GCM/NoPadding 解密
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * 根据密码生成 AES 密钥
     *
     * @param password 密码
     * @return AES 密钥
     * @throws Exception 发生异常时抛出
     */
    private static SecretKey generateSecretKey(String password) throws Exception {
        // 使用密码生成 AES 密钥（截取固定长度）
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] keyMaterial = new byte[32]; // 256 位密钥
        System.arraycopy(keyBytes, 0, keyMaterial, 0, Math.min(keyBytes.length, keyMaterial.length));
        return new SecretKeySpec(keyMaterial, "AES");
    }
}
