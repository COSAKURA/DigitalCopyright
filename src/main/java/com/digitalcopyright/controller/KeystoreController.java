package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.R;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/keystore")
public class KeystoreController {

    @Autowired
    private KeystoreService keystoreService;

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 创建区块链地址并生成 Keystore文件
     *
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @return 用户的区块链地址
     */
    @GetMapping("/generateKeystore")
    public R generateKeystore(@RequestParam String email, @RequestParam String password) {
        try {
            // 调用服务生成 Keystore 并更新用户信息
            String userAddress = keystoreService.generateKeystoreAndUpdateUser(email, password);
            return R.ok().put("address", userAddress);
        } catch (Exception e) {
            // 返回错误响应
            return R.error(400, "生成 Keystore 文件失败: " + e.getMessage());
        }
    }


    /**
     * 上传私钥文件
     *
     * @param email    用户邮箱
     * @param password 用户输入的密码
     * @param file     私钥文件
     * @return 用户的区块链地址和私钥
     */
    @PostMapping("/uploadKeystore")
    public R uploadKeystore(@RequestParam("file") MultipartFile file,
                            @RequestParam("email") String email,
                            @RequestParam("password") String password) {
        try {
            // 验证并解析 Keystore 文件
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email);
            UsersDO user = usersMapper.selectOne(queryWrapper);

            if (user == null) {
                return R.error(400, "用户不存在");
            }

            // 调用服务验证私钥文件
            String privateKey = keystoreService.loadKeyPairFromKeystore(file, password, user.getBlockchainAddress());

            // 返回验证成功的响应，包含用户私钥
            return Objects.requireNonNull(Objects.requireNonNull(R.ok()
                                    .put("message", "验证成功"))
                            .put("address", user.getBlockchainAddress()))
                    .put("privateKey", privateKey);

        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error(500, "解析 Keystore 文件失败: " + e.getMessage());
        }
    }
}
