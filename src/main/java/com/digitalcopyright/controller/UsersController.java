package com.digitalcopyright.controller;


import com.digitalcopyright.model.DTO.BindBlockchainAddressDTO;
import com.digitalcopyright.service.KeystoreService;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private UsersService usersService;

    @Autowired
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

}

