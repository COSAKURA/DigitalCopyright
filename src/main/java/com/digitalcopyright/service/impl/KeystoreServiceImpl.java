package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.utils.KeystoreUtils;
import jakarta.annotation.Resource;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author Sakura
 */
@Service
public class KeystoreServiceImpl implements KeystoreService {

    @Resource
    private CryptoSuite cryptoSuite;

    @Resource
    private UsersMapper usersMapper;

    private static final String BASE_PATH = new File("src/main/resources/privateKey").getAbsolutePath() + "/";


    /**
     * 生成 Keystore 并更新用户的区块链地址。
     *
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @return 用户的区块链地址
     */
    @Override
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

        // 生成 Keystore 文件内容（加密后的私钥）
        String privateKey = KeystoreUtils.generateKeystore(keyPair, password);

        // 更新用户表
        user.setBlockchainAddress(userAddress);
        usersMapper.updateById(user);

        // 返回加密后的私钥内容
        return privateKey;
    }

    /**
     * 验证用户上传的 Keystore 文件并返回私钥。
     *
     * @param file              用户上传的 Keystore 文件
     * @param password          解密密码
     * @param blockchainAddress 待验证的区块链地址
     * @return 用户的私钥字符串
     */
    @Override
    public String loadKeyPairFromKeystore(MultipartFile file, String password, String blockchainAddress) throws IOException {
        // 保存上传文件到临时路径
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = "keystore-" + System.currentTimeMillis() + ".json";
        Path filePath = Paths.get(tempDir, filename);
        Files.write(filePath, file.getBytes());

        try {
            // 加载 Keystore 文件并解析私钥
            String userPrivateKey = KeystoreUtils.loadPrivateKeyFromKeystore(filePath.toString(), password);

            // 创建 KeyPair 并验证区块链地址
            CryptoKeyPair keyPair = cryptoSuite.createKeyPair(userPrivateKey);
            if (!keyPair.getAddress().equals(blockchainAddress)) {
                throw new IllegalArgumentException("验证失败：上传的 Keystore 文件与区块链地址不匹配");
            }

            // 返回私钥字符串
            return userPrivateKey;
        } catch (Exception e) {
            throw new RuntimeException("解析 Keystore 文件失败: " + e.getMessage(), e);
        } finally {
            // 删除临时文件
            Files.deleteIfExists(filePath);
        }
    }

}
