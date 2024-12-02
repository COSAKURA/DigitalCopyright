package com.digitalcopyright;

import com.digitalcopyright.utils.KeystoreUtils;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.UUID;

@SpringBootTest
public class GenerateKeystoreExampleTest {

    @Autowired
    private Client client;

    @Test
    public void saveKeystoreExample() throws Exception {
        // 初始化区块链客户端，生成密钥对
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();

        // 基础路径
        String basePath = "D:/数字创意作品链上版权认证与交易平台/私钥";

        // 检查并创建文件夹
        File directory = new File(basePath);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("无法创建目录: " + basePath);
            }
        }

        String keystorePath = basePath + "/keystore-" + keyPair.getAddress() + ".json";

        // 用户输入密码
        String password = "sakura0000001";

        // 生成 Keystore 文件
        KeystoreUtils.generateKeystore(keyPair, password, keystorePath);

        // 输出生成的 Keystore 文件路径
        System.out.println("Keystore 文件已保存至: " + keystorePath);
        System.out.println("生成的地址: " + keyPair.getAddress());
    }

    @Test
    public void generateKeystoreExample() throws Exception {
        KeystoreUtils.generateKeystore(client.getCryptoSuite().createKeyPair(), "sakura0000001", "D:/数字创意作品链上版权认证与交易平台/私钥/keystore-" + UUID.randomUUID() + ".json");
    }
    @Test
    public void loadPrivateKeyFromKeystore() throws Exception {
        KeystoreUtils.loadPrivateKeyFromKeystore("D:/数字创意作品链上版权认证与交易平台/私钥/keystore-b045f7af-8eb6-4cf9-9551-ad00d1debf7f.json", "sakura0000001");

    }
}
