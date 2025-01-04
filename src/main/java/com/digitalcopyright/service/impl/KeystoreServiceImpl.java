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

/**
 * Keystore 服务实现类
 * 提供 Keystore 文件的生成、验证等功能，并更新用户的区块链地址。
 * 作者: Sakura
 */
@Service
public class KeystoreServiceImpl implements KeystoreService {

    @Resource
    private CryptoSuite cryptoSuite; // 区块链加密套件，用于生成和验证密钥对

    @Resource
    private UsersMapper usersMapper; // 用户表操作类

    private static final String BASE_PATH = new File("src/main/resources/privateKey").getAbsolutePath() + "/";

    /**
     * 生成 Keystore 并更新用户的区块链地址
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @return 用户的区块链地址
     */
    @Override
    public String generateKeystoreAndUpdateUser(String email, String password) throws Exception {
        // 1. 查询用户信息
        QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UsersDO user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            // 如果用户不存在，抛出异常
            throw new IllegalArgumentException("用户不存在");
        }

        // 2. 创建区块链密钥对
        CryptoKeyPair keyPair = cryptoSuite.createKeyPair(); // 生成新的密钥对
        String userAddress = keyPair.getAddress();           // 获取区块链地址

        // 3. 生成 Keystore 文件内容（加密后的私钥）
        String privateKey = KeystoreUtils.generateKeystore(keyPair, password);

        // 4. 更新用户表的区块链地址
        user.setBlockchainAddress(userAddress);
        usersMapper.updateById(user); // 更新用户信息

        // 5. 返回加密后的私钥内容（供客户端保存）
        return privateKey;
    }

    /**
     * 验证用户上传的 Keystore 文件并返回私钥
     *
     * @param file              用户上传的 Keystore 文件
     * @param password          解密密码
     * @param blockchainAddress 待验证的区块链地址
     * @return 用户的私钥字符串
     */
    @Override
    public String loadKeyPairFromKeystore(MultipartFile file, String password, String blockchainAddress) throws IOException {
        // 1. 保存上传的 Keystore 文件到临时路径
        String tempDir = System.getProperty("java.io.tmpdir"); // 获取系统临时目录
        String filename = "keystore-" + System.currentTimeMillis() + ".json"; // 临时文件名
        Path filePath = Paths.get(tempDir, filename); // 文件路径
        Files.write(filePath, file.getBytes()); // 写入文件内容

        try {
            // 2. 加载 Keystore 文件并解析私钥
            String userPrivateKey = KeystoreUtils.loadPrivateKeyFromKeystore(filePath.toString(), password);

            // 3. 创建 KeyPair 并验证区块链地址
            CryptoKeyPair keyPair = cryptoSuite.createKeyPair(userPrivateKey);
            if (!keyPair.getAddress().equals(blockchainAddress)) {
                // 如果解析的地址与传入的地址不匹配，抛出异常
                throw new IllegalArgumentException("验证失败：上传的 Keystore 文件与区块链地址不匹配");
            }

            // 4. 返回私钥字符串
            return userPrivateKey;
        } catch (Exception e) {
            // 捕获解析 Keystore 文件的异常并抛出运行时异常
            throw new RuntimeException("解析 Keystore 文件失败: " + e.getMessage(), e);
        } finally {
            // 5. 删除临时文件，确保文件不被泄漏
            Files.deleteIfExists(filePath);
        }
    }
}
