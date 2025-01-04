package com.digitalcopyright.controller;

import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

/**
 * 用户控制器
 * 提供与用户相关的操作，包括绑定区块链地址和获取用户信息。
 * 功能：
 * 1. 绑定区块链地址到用户账号。
 * 2. 获取用户详细信息。
 * @author Sakura
 * @since 2024-11-27
 */
@RestController
@RequestMapping("/user")
public class UsersController {

    @Resource
    private UsersService usersService; // 注入用户服务，处理用户相关操作

    @Resource
    private KeystoreService keystoreService; // 注入 Keystore 服务，处理私钥相关操作

    /**
     * 绑定区块链地址
     * 此接口用于验证用户上传的私钥文件，并将区块链地址绑定到指定邮箱用户。
     * @param file 上传的 Keystore 文件
     * @param email 用户邮箱
     * @param password 用户密码，用于解密私钥
     * @param blockchainAddress 区块链地址
     * @return 绑定结果，包括区块链地址和私钥
     */
    @PostMapping("/bindBlockchainAddress")
    public R bindBlockchainAddress(@RequestParam("file") MultipartFile file,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("blockchainAddress") String blockchainAddress) {
        try {
            // 调用 KeystoreService 验证私钥文件是否与区块链地址匹配
            String privateKey = keystoreService.loadKeyPairFromKeystore(file, password, blockchainAddress);

            // 验证通过后，将区块链地址绑定到用户
            usersService.bindBlockchainAddress(email, blockchainAddress);

            // 返回成功结果，包括绑定的区块链地址和解密后的私钥
            return Objects.requireNonNull(R.ok()
                            .put("message", "区块链地址绑定成功")
                            .put("blockchainAddress", blockchainAddress))
                    .put("privateKey", privateKey);
        } catch (IllegalArgumentException e) {
            // 捕获参数校验异常，返回 400 错误
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            // 捕获未知异常，返回 500 错误
            return R.error(500, "绑定区块链地址失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     * 根据用户邮箱查询用户详细信息。
     * @param email 用户邮箱
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public R getUserInfo(@RequestParam("email") String email) {
        try {
            // 调用 UsersService 查询用户信息
            UsersDO user = usersService.getUserByEmail(email);

            // 如果用户不存在，返回 404 错误
            if (user == null) {
                return R.error(404, "用户不存在");
            }

            // 返回用户信息
            return R.ok().put("user", user);
        } catch (Exception e) {
            // 捕获异常，返回 500 错误
            return R.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }
}
