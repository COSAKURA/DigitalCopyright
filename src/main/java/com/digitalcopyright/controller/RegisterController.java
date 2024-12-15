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
 * @author Sakura
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class RegisterController {

    private final UsersService usersService;
    private final MailService mailService;

    // 用于验证码存储的内存
    private final Map<String, EmailCodeDTO> emailCodeStore = new HashMap<>();


    // 用于限制请求次数和验证码有效时间
    @Value("${spring.mail.limit}")
    private int limit;

    @Value("${spring.mail.limitTime}")
    private int limitTime;

    @Autowired
    public RegisterController(UsersService usersService, MailService mailService) {
        this.usersService = usersService;
        this.mailService = mailService;
    }

    /**
     * 发送邮箱验证码
     * @param request 包含邮箱地址
     * @return 返回验证码发送结果
     */
    @PostMapping("/emailCode")
    public R emailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        log.info("邮箱：{}",email);
        long currentTime = System.currentTimeMillis();

        // 检查内存中是否存在该邮箱的验证码记录
        if (emailCodeStore.containsKey(email)) {
            EmailCodeDTO emailCode = emailCodeStore.get(email);

            // 校验验证码是否超时
            if (currentTime - emailCode.getTimestamp() > limitTime) {
                // 验证码超时，移除并返回错误
                emailCodeStore.remove(email);
                return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
            }

            // 如果验证码请求次数超过限制，返回错误
            if (emailCode.getTimes() >= limit) {
                return R.error(BizCodeEnum.OVER_REQUESTS.getCode(), BizCodeEnum.OVER_REQUESTS.getMsg());
            }

            // 更新验证码及请求次数
            String code = CodeUtils.creatCode(6);
            emailCode.setEmailCode(code);
            emailCode.setTimes(emailCode.getTimes() + 1);
            emailCode.setTimestamp(currentTime);
            emailCodeStore.put(email, emailCode);

            // 发送验证码邮件
            mailService.sendCodeMailMessage(email, code);
        } else {
            // 检查数据库中是否存在该用户名
            UsersDO user = usersService.getOne(new QueryWrapper<UsersDO>().eq("email", email));
            if (user != null) {
                return R.error(BizCodeEnum.HAS_USERNAME.getCode(), BizCodeEnum.HAS_USERNAME.getMsg());
            }

            // 生成新的验证码
            String code = CodeUtils.creatCode(6);
            EmailCodeDTO emailCod = new EmailCodeDTO(code, email, 1, currentTime);
            emailCodeStore.put(email, emailCod);

            // 发送验证码邮件
            mailService.sendCodeMailMessage(email, code);
        }

        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg());
    }


    /**
     * 注册
     * @param register 包含邮箱、密码、验证码
     * @return 返回注册结果
     */
    @PostMapping("/register")
    public R register(@RequestBody RegisterDTO register) {

        String email = register.getEmail().trim();
        String emailCode = register.getEmailCode().trim();

        // 检验验证码是否存在
        if (!emailCodeStore.containsKey(email)) {
            return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
        }

        EmailCodeDTO storedEmailCode = emailCodeStore.get(email);

        // 校验验证码是否超时
        long currentTime = System.currentTimeMillis();
        // 验证码有效期为5分钟
        if (currentTime - storedEmailCode.getTimestamp() > 5 * 60 * 1000) {
            emailCodeStore.remove(email);
            return R.error(BizCodeEnum.OVER_TIME.getCode(), BizCodeEnum.OVER_TIME.getMsg());
        }

        // 校验邮箱号是否匹配
        if (!email.equals(storedEmailCode.getEmail())) {
            return R.error(BizCodeEnum.BAD_DOING.getCode(), BizCodeEnum.BAD_DOING.getMsg());
        }

        // 校验验证码是否正确
        if (!emailCode.equals(storedEmailCode.getEmailCode())) {
            return R.error(BizCodeEnum.BAD_EMAILCODE_VERIFY.getCode(), BizCodeEnum.BAD_EMAILCODE_VERIFY.getMsg());
        }

        // 验证通过，封装用户信息并存储
        UsersDO user = new UsersDO();
        user.setEmail(email);
        user.setUsername(register.getUsername());
        user.setPassword(SecurityUtils.encodePassword(register.getPassword().trim()));
        user.setType(register.getType());
        user.setCreatedAt(DateUtils.getCurrentTime());
        user.setStatus("正常");
        usersService.save(user);

        // 注册成功后，删除验证码记录
        emailCodeStore.remove(email);

        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg());
    }

}
