package com.digitalcopyright.service;

import com.digitalcopyright.fisco.DigitalCopyright;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple10;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class WorkDetailsService {

    @Autowired
    private Client client;

    private static final String CONTRACT_ADDRESS = "0xddc08e99560af0b2f0a3431d6c2c010ff1492d48";

    /**
     * 查询链上作品详情
     *
     * @param keyPair 用户的 KeyPair
     * @param workId  作品 ID
     * @return 作品详情
     * @throws Exception 查询失败时抛出异常
     */
    public Tuple10<BigInteger, String, String, String, String, String, BigInteger, String, BigInteger, Boolean> getWorkDetails(CryptoKeyPair keyPair, BigInteger workId) throws Exception {
        // 加载合约
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, keyPair);

        // 调用 getWorkDetails 方法
        return digitalCopyright.getWorkDetails(workId);
    }
}
