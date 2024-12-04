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
 * @author Sakura
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class LoginController {

    private final UsersService usersService;

    @Autowired
    public LoginController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * 用户登录接口
     *
     * @param login 登录信息
     * @return 登录结果
     */
    @PostMapping("/login")
    public R login(@RequestBody LoginDTO login) {
        log.info("用户登录: {}", login.getEmail());
        try {
            return usersService.login(login);
        } catch (IllegalArgumentException e) {
            return R.error(BizCodeEnum.BAD_PUTDATA.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return R.error(500, "系统错误，请稍后再试");
        }
    }
}
