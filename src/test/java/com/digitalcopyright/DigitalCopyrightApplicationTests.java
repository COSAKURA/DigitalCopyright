package com.digitalcopyright;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.WorksMapper;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.utils.KeystoreUtils;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple10;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Slf4j
@SpringBootTest
class DigitalCopyrightApplicationTests {

    @Autowired
    private Client client1;

    @Autowired
    private WorksMapper worksMapper;

    @Test
    public void testGetWorkDetailsWithUserKey() throws Exception {
        // 用户提供的地址和私钥
        String userPrivateKey = KeystoreUtils.loadPrivateKeyFromKeystore("D:/数字创意作品链上版权认证与交易平台/私钥/keystore-8e6c9d20" +
                "-3b2c-490a-94c8-dbda89b81222.json", "wyh123456");

        // 使用用户私钥创建 KeyPair
        CryptoSuite cryptoSuite = client1.getCryptoSuite();
        CryptoKeyPair userKeyPair = cryptoSuite.createKeyPair(userPrivateKey);

        // 验证用户地址
        String userAddress = userKeyPair.getAddress();
        System.out.println("用户地址: " + userAddress);

        // 合约地址
        String contractAddress = "0xddc08e99560af0b2f0a3431d6c2c010ff1492d48";

        // 加载合约
        DigitalCopyright digitalCopyright = DigitalCopyright.load(contractAddress, client1, userKeyPair);

        // 调用 getWorkDetails 方法
        Tuple10<BigInteger, String, String, String, String, String, BigInteger, String, BigInteger, Boolean> details =
                digitalCopyright.getWorkDetails(BigInteger.valueOf(1));

        // 打印返回结果
        System.out.println("Work ID: " + details.getValue1());
        System.out.println("Title: " + details.getValue2());
        System.out.println("Description: " + details.getValue3());
        System.out.println("Work Hash: " + details.getValue4());
        System.out.println("Current Owner: " + details.getValue5());
        System.out.println("Reviewer: " + details.getValue6());
        System.out.println("Status: " + details.getValue7());
        System.out.println("Copyright Certificate: " + details.getValue8());
        System.out.println("Created At: " + details.getValue9());
        System.out.println("Is On Auction: " + details.getValue10());
    }


}
