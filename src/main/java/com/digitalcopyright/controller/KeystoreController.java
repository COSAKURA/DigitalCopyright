package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.utils.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Keystore 控制器
 * 此控制器提供与用户密钥管理相关的功能，包括生成加密私钥文件、上传和验证私钥文件。
 * 主要功能：
 * 1. 生成加密后的私钥文件并返回给用户。
 * 2. 验证用户上传的私钥文件是否与区块链地址匹配。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/keystore")
@Slf4j
public class KeystoreController {

    @Resource
    private KeystoreService keystoreService; // 注入 Keystore 服务，用于处理密钥相关逻辑

    @Resource
    private UsersMapper usersMapper; // 注入用户 Mapper，用于数据库用户信息查询

    /**
     * 生成密钥对并返回加密后的私钥文件
     * 此接口用于生成一个新的密钥对，并将加密后的私钥保存到用户记录中，同时以文件形式返回给用户下载。
     * @param email 用户邮箱，用于标识生成密钥的用户
     * @param password 用户密码，用于加密私钥
     * @return 加密后的私钥文件，作为 JSON 文件供用户下载
     */
    @GetMapping("/generateKeystore")
    public ResponseEntity<byte[]> generateKeystore(@RequestParam String email, @RequestParam String password) {
        try {
            // 调用 Service 层方法，生成密钥对并返回加密后的私钥内容
            String encryptedPrivateKey = keystoreService.generateKeystoreAndUpdateUser(email, password);

            // 将加密后的私钥内容封装为 JSON 格式
            String jsonContent = String.format(encryptedPrivateKey);
            log.info("生成的加密私钥内容：{}", encryptedPrivateKey);

            // 设置响应头，指定内容类型为 JSON，文件名为 "encrypted-private-key.json"
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("encrypted-private-key.json", StandardCharsets.UTF_8)
                    .build());

            // 返回包含 JSON 文件内容的响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("生成加密私钥失败", e); // 打印错误日志
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("生成加密私钥失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 验证用户上传的私钥文件
     * 此接口用于验证用户上传的 Keystore 文件是否有效，并解析私钥。
     * @param email 用户邮箱，用于标识验证的用户
     * @param password 用户输入的密码，用于解密 Keystore 文件
     * @param file 用户上传的 Keystore 文件
     * @return 验证结果，包括区块链地址和解密后的私钥
     */
    @PostMapping("/uploadKeystore")
    public R uploadKeystore(@RequestParam("file") MultipartFile file,
                            @RequestParam("email") String email,
                            @RequestParam("password") String password) {
        try {
            // 查询用户信息
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", email); // 根据邮箱查询用户
            UsersDO user = usersMapper.selectOne(queryWrapper);

            // 如果用户不存在，返回错误信息
            if (user == null) {
                return R.error(400, "用户不存在");
            }

            // 调用服务层方法，验证 Keystore 文件并解析私钥
            String privateKey = keystoreService.loadKeyPairFromKeystore(file, password, user.getBlockchainAddress());

            // 返回验证成功的响应，包含用户区块链地址和私钥
            return Objects.requireNonNull(R.ok()
                            .put("message", "验证成功")
                            .put("address", user.getBlockchainAddress()))
                            .put("privateKey", privateKey);
        } catch (IllegalArgumentException e) {
            // 参数校验异常，返回 400 错误
            log.warn("上传的 Keystore 文件无效: {}", e.getMessage());
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            // 其他异常，返回 500 错误
            log.error("解析 Keystore 文件失败", e);
            return R.error(500, "解析 Keystore 文件失败: " + e.getMessage());
        }
    }
}
