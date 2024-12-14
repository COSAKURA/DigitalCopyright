package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;

import com.digitalcopyright.utils.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;


import java.util.Objects;

/**
 * @author Sakura
 */
@RestController
@RequestMapping("/keystore")
@Slf4j
public class KeystoreController {

    @Resource
    private KeystoreService keystoreService;

    @Resource
    private UsersMapper usersMapper;

    @GetMapping("/generateKeystore")
    public ResponseEntity<byte[]> generateKeystore(@RequestParam String email, @RequestParam String password) {
        try {
            // 调用 Service 获取加密后的私钥内容
            String encryptedPrivateKey = keystoreService.generateKeystoreAndUpdateUser(email, password);

            // 将加密私钥封装成 JSON 格式
            String jsonContent = String.format(encryptedPrivateKey);
            log.error("私钥：{}",encryptedPrivateKey);

            // 设置响应头，提示浏览器下载
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("encrypted-private-key.json", StandardCharsets.UTF_8)
                    .build());

            // 返回 JSON 文件内容
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(jsonContent.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // 返回错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("生成加密私钥失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }


    /**
     * 验证私钥文件
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
