package com.digitalcopyright.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DTO.EmailCodeDTO;
import com.digitalcopyright.model.DTO.RegisterDTO;
import com.digitalcopyright.service.MailService;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.CodeUtils;
import com.digitalcopyright.utils.R;
import com.digitalcopyright.utils.SecurityUtils;
import com.digitalcopyright.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册控制器
 * 提供用户注册相关的接口功能：
 * 1. 发送邮箱验证码。
 * 2. 验证验证码并完成注册。
 * 使用内存存储验证码信息，同时限制验证码的有效时间和请求次数。
 * @author Sakura
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class RegisterController {

    private final UsersService usersService; // 注入用户服务，处理用户相关操作
    private final MailService mailService;  // 注入邮件服务，发送验证码邮件

    // 用于存储邮箱验证码信息的内存存储
    private final Map<String, EmailCodeDTO> emailCodeStore = new HashMap<>();

    // 验证码请求限制和有效时间配置
    @Value("${spring.mail.limit}")
    private int limit; // 每个邮箱的最大验证码请求次数
    @Value("${spring.mail.limitTime}")
    private int limitTime; // 验证码请求间隔时间（毫秒）

    @Autowired
    public RegisterController(UsersService usersService, MailService mailService) {
        this.usersService = usersService;
        this.mailService = mailService;
    }

    /**
     * 发送邮箱验证码
     * 根据用户邮箱生成验证码并发送到指定邮箱，同时限制请求频率和验证码有效时间。
     * @param request 请求体，包含邮箱地址
     * @return 响应结果，包括发送成功或失败信息
     */
    @PostMapping("/emailCode")
    public R emailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email"); // 从请求体获取邮箱地址
        log.info("发送验证码到邮箱: {}", email);
        long currentTime = System.currentTimeMillis(); // 获取当前时间

        // 检查内存中是否已有该邮箱的验证码记录
        if (emailCodeStore.containsKey(email)) {
            EmailCodeDTO emailCode = emailCodeStore.get(email);

            // 验证码是否超时
            if (currentTime - emailCode.getTimestamp() > limitTime) {
                emailCodeStore.remove(email); // 删除超时的验证码记录
                return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
            }

            // 检查请求次数是否超过限制
            if (emailCode.getTimes() >= limit) {
                return R.error(BizCodeEnum.OVER_REQUESTS.getCode(), BizCodeEnum.OVER_REQUESTS.getMsg());
            }

            // 生成新验证码并更新记录
            String code = CodeUtils.creatCode(6);
            emailCode.setEmailCode(code);
            emailCode.setTimes(emailCode.getTimes() + 1);
            emailCode.setTimestamp(currentTime);
            emailCodeStore.put(email, emailCode);

            // 发送验证码邮件
            mailService.sendCodeMailMessage(email, code);
        } else {
            // 如果没有记录，则检查数据库中是否已注册该邮箱
            UsersDO user = usersService.getOne(new QueryWrapper<UsersDO>().eq("email", email));
            if (user != null) {
                return R.error(BizCodeEnum.HAS_USERNAME.getCode(), BizCodeEnum.HAS_USERNAME.getMsg());
            }

            // 生成新验证码并保存到内存
            String code = CodeUtils.creatCode(6);
            EmailCodeDTO emailCode = new EmailCodeDTO(code, email, 1, currentTime);
            emailCodeStore.put(email, emailCode);

            // 发送验证码邮件
            mailService.sendCodeMailMessage(email, code);
        }

        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg());
    }

    /**
     * 用户注册
     * 验证用户提交的邮箱、密码和验证码，完成用户注册流程。
     * @param register 包含注册信息（邮箱、密码、验证码等）的 DTO
     * @return 注册结果，包括成功或失败信息
     */
    @PostMapping("/register")
    public R register(@RequestBody RegisterDTO register) {
        String email = register.getEmail().trim(); // 获取并去除邮箱中的空白字符
        String emailCode = register.getEmailCode().trim(); // 获取并去除验证码中的空白字符

        // 验证验证码是否存在
        if (!emailCodeStore.containsKey(email)) {
            return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
        }

        EmailCodeDTO storedEmailCode = emailCodeStore.get(email);

        // 检查验证码是否超时（有效期为 5 分钟）
        long currentTime = System.currentTimeMillis();
        if (currentTime - storedEmailCode.getTimestamp() > 5 * 60 * 1000) {
            emailCodeStore.remove(email); // 删除超时的验证码记录
            return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
        }

        // 检查邮箱是否匹配
        if (!email.equals(storedEmailCode.getEmail())) {
            return R.error(BizCodeEnum.BAD_DOING.getCode(), BizCodeEnum.BAD_DOING.getMsg());
        }

        // 检查验证码是否正确
        if (!emailCode.equals(storedEmailCode.getEmailCode())) {
            return R.error(BizCodeEnum.BAD_EMAILCODE_VERIFY.getCode(), BizCodeEnum.BAD_EMAILCODE_VERIFY.getMsg());
        }

        // 验证通过后保存用户信息
        UsersDO user = new UsersDO();
        user.setEmail(email);
        user.setUsername(register.getUsername());
        user.setPassword(SecurityUtils.encodePassword(register.getPassword().trim())); // 加密存储密码
        user.setType(register.getType());
        user.setCreatedAt(DateUtils.getCurrentTime()); // 设置创建时间
        user.setStatus("正常");
        usersService.save(user);

        // 注册成功后删除验证码记录
        emailCodeStore.remove(email);

        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg());
    }
}
