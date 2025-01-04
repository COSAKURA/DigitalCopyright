package com.digitalcopyright.controller;

import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.model.DTO.LoginDTO;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 登录控制器
 * 此控制器用于处理用户登录请求，调用服务层验证用户登录信息。
 * 提供的功能：
 * 1. 用户通过邮箱和密码进行登录。
 * 2. 返回登录结果，包括成功的用户信息或失败的错误提示。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class LoginController {

    private final UsersService usersService; // 注入用户服务，用于处理登录逻辑

    /**
     * 构造函数
     * 使用构造器注入 `UsersService` 服务
     * @param usersService 注入的用户服务实例
     */
    @Autowired
    public LoginController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * 用户登录接口
     * 此接口通过用户提交的邮箱和密码验证用户身份。
     * @param login 包含用户登录信息的 DTO，包括邮箱和密码
     * @return 登录结果，包含登录成功或失败的信息
     */
    @PostMapping("/login")
    public R login(@RequestBody LoginDTO login) {
        log.info("用户尝试登录: {}", login.getEmail()); // 打印用户登录尝试的日志
        try {
            // 调用服务层的 `login` 方法，验证用户登录信息
            return usersService.login(login);
        } catch (IllegalArgumentException e) {
            // 捕获参数校验异常，例如邮箱格式错误或密码为空
            log.warn("登录参数错误: {}", e.getMessage());
            return R.error(BizCodeEnum.BAD_PUTDATA.getCode(), e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常
            log.error("登录失败: {}", e.getMessage(), e);
            return R.error(500, "系统错误，请稍后再试");
        }
    }
}
