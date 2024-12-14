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
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@RestController
@RequestMapping("/user")
public class UsersController {
    @Resource
    private UsersService usersService;

    @Resource
    private KeystoreService keystoreService;

    /**
     * 绑定区块链地址
     *
     * @return 操作结果
     */
    @PostMapping("/bindBlockchainAddress")
    public R bindBlockchainAddress(@RequestParam("file") MultipartFile file,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("blockchainAddress") String blockchainAddress) {
        try {
            // 调用工具类验证私钥文件与区块链地址是否匹配
            String privateKey = keystoreService.loadKeyPairFromKeystore(file, password, blockchainAddress);

            // 验证成功后绑定区块链地址
            usersService.bindBlockchainAddress(email, blockchainAddress);

            return Objects.requireNonNull(Objects.requireNonNull(R.ok()
                                    .put("message", "区块链地址绑定成功"))
                            .put("blockchainAddress", blockchainAddress))
                    .put("privateKey", privateKey);
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error(500, "绑定区块链地址失败: " + e.getMessage());
        }
    }


    /**
     * 获取用户信息
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public R getUserInfo(@RequestParam("email") String email) {
        try {
            // 调用 Service 获取用户信息
            UsersDO user = usersService.getUserByEmail(email);

            if (user == null) {
                return R.error(404, "用户不存在");
            }

            // 返回用户信息
            return R.ok().put("user", user);
        } catch (Exception e) {
            return R.error(500, "获取用户信息失败: " + e.getMessage());
        }
    }
}

