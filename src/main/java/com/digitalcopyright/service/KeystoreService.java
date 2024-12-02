package com.digitalcopyright.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.utils.KeystoreUtils;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 服务类：提供 Keystore 生成和解析功能。
 */
@Service
public class KeystoreService {

    @Autowired
    private CryptoSuite cryptoSuite;

    @Autowired
    private UsersMapper usersMapper;

    private static final String BASE_PATH = "D:/数字创意作品链上版权认证与交易平台/私钥/";

    /**
     * 生成 Keystore 并更新用户的区块链地址。
     *
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @return 用户的区块链地址
     * @throws Exception 如果生成失败，抛出异常
     */
    public String generateKeystoreAndUpdateUser(String email, String password) throws Exception {
        // 查询用户
        QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UsersDO user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 创建 KeyPair
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair();
        String userAddress = keyPair.getAddress();

        // 生成 Keystore 文件
        String uuid = UUID.randomUUID().toString();
        String keystorePath = BASE_PATH + "keystore-" + uuid + ".json";
        KeystoreUtils.generateKeystore(keyPair, password, keystorePath);

        // 更新用户表
        user.setBlockchainAddress(userAddress);
        usersMapper.updateById(user);

        return userAddress;
    }

    /**
     * 加载 Keystore 文件并解析私钥。
     *
     * @param keystorePath Keystore 文件路径
     * @param password     解密密码
     * @return 用户的 KeyPair
     * @throws Exception 如果解析失败，抛出异常
     */
    public CryptoKeyPair loadKeyPairFromKeystore(String keystorePath, String password) throws Exception {
        String userPrivateKey = KeystoreUtils.loadPrivateKeyFromKeystore(keystorePath, password);
        return cryptoSuite.createKeyPair(userPrivateKey);
    }
}
