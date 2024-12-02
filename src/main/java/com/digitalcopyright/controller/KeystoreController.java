package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.service.UsersService;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class KeystoreController {

    @Autowired
    private KeystoreService keystoreService;

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 生成 Keystore 文件并更新用户信息。
     *
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @return 用户的区块链地址
     */
    @GetMapping("/generateKeystore")
    public ResponseEntity<Map<String, Object>> generateKeystore(@RequestParam String email,
                                                                @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userAddress = keystoreService.generateKeystoreAndUpdateUser(email, password);
            response.put("address", userAddress);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "生成 Keystore 文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 用户上传 Keystore 文件并解析私钥。
     *
     * @param file     Keystore 文件
     * @param password 解密密码
     * @return 用户地址
     */
    @PostMapping("/uploadKeystore")
    public ResponseEntity<Map<String, Object>> uploadKeystore(@RequestParam("file") MultipartFile file,
                                                              @RequestParam("password") String password,
                                                              @RequestParam("email") String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 保存上传文件到临时路径
            String tempDir = System.getProperty("java.io.tmpdir");
            String filename = "keystore-" + System.currentTimeMillis() + ".json";
            Path filePath = Paths.get(tempDir, filename);
            Files.write(filePath, file.getBytes());

            // 加载 Keystore 文件
            CryptoKeyPair keyPair = keystoreService.loadKeyPairFromKeystore(filePath.toString(), password);

            // 删除临时文件
            Files.delete(filePath);

            // 验证用户地址是否与数据库中存储的地址一致
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            UsersDO user = usersMapper.selectOne(queryWrapper);

            if (user == null) {
                response.put("error", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }

            if (!keyPair.getAddress().equals(user.getBlockchainAddress())) {
                response.put("error", "验证失败：上传的 Keystore 文件与用户的区块链地址不匹配");
                return ResponseEntity.badRequest().body(response);
            }

            // 返回验证成功的响应
            response.put("message", "验证成功");
            response.put("address", keyPair.getAddress());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "解析 Keystore 文件失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
