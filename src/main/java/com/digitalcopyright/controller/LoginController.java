package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.model.DTO.LoginDTO;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sakura
 */
@RestController
@RequestMapping("/api")
public class LoginController {

    private UsersService usersService;

    // 使用构造函数注入所有依赖
    @Autowired
    public void registerController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/login")
    public R login(@RequestBody LoginDTO login) {
        // 获取邮箱和密码
        String email = login.getEmail();
        String password = login.getPassword();
        // 去除密码中的空格
        password = password.replaceAll(" ", "");
        email = email.replaceAll(" ", "");
        // 查询数据库中是否存在该用户
        UsersDO user = usersService.getOne(
                new QueryWrapper<UsersDO>().eq("email", email)
        );
        if (user != null) {
            // 使用BCrypt密码加密算法验证密码是否正确。
            if (SecurityUtils.matchesPassword(password, user.getPassword())) {
                // 验证成功，生成JWT token，代表用户的登录状态
                String token = JwtTokenUtil.generateToken(user);
                // 成功响应，返回生成的登录Token。
                return R.ok(BizCodeEnum.SUCCESSFUL.getMsg()).put("loginToken", token);
            } else {
                // 密码不匹配，返回BAD_PUTDATA错误，表示密码错误。
                return R.error(BizCodeEnum.BAD_PUTDATA.getCode(), BizCodeEnum.BAD_PUTDATA.getMsg());
            }
        } else {
            // 查询不到该用户，返回NO_SUCHUSER错误，表示用户不存在。
            return R.error(BizCodeEnum.NO_SUCHUSER.getCode(), BizCodeEnum.NO_SUCHUSER.getMsg());
        }
    }
}
