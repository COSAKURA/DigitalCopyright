package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DTO.LoginDTO;
import com.digitalcopyright.service.UsersService;
import com.digitalcopyright.utils.JwtTokenUtil;
import com.digitalcopyright.utils.R;
import com.digitalcopyright.utils.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 提供用户登录、绑定区块链地址、查询用户信息等功能
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, UsersDO> implements UsersService {

    @Resource
    private UsersMapper usersMapper; // 用户表操作类

    /**
     * 用户登录服务
     * 根据邮箱和密码验证用户，并生成登录 Token
     *
     * @param login 包含邮箱和密码的登录信息
     * @return 登录结果，包含登录 Token 或错误信息
     */
    @Override
    public R login(LoginDTO login) {
        // 获取邮箱和密码并去除空格
        String email = login.getEmail().trim();
        String password = login.getPassword().trim();

        // 查询用户信息
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null) {
            // 如果用户不存在，返回错误
            return R.error(BizCodeEnum.NO_SUCHUSER.getCode(), BizCodeEnum.NO_SUCHUSER.getMsg());
        }

        // 验证密码是否匹配
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            // 如果密码不匹配，返回错误
            return R.error(BizCodeEnum.BAD_PUTDATA.getCode(), BizCodeEnum.BAD_PUTDATA.getMsg());
        }

        // 生成登录 Token
        String token = JwtTokenUtil.generateToken(user);

        // 返回成功结果，包含登录 Token
        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg()).put("loginToken", token);
    }

    /**
     * 绑定区块链地址服务
     * 根据邮箱查找用户并绑定指定的区块链地址
     *
     * @param email 用户邮箱
     * @param blockchainAddress 区块链地址
     */
    @Override
    public void bindBlockchainAddress(String email, String blockchainAddress) {
        // 根据邮箱查找用户
        QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UsersDO user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            // 如果用户不存在，抛出异常
            throw new IllegalArgumentException("用户不存在");
        }

        // 绑定区块链地址
        user.setBlockchainAddress(blockchainAddress);
        int updateResult = usersMapper.updateById(user);

        if (updateResult <= 0) {
            // 如果更新失败，抛出运行时异常
            throw new RuntimeException("区块链地址绑定失败");
        }
    }

    /**
     * 根据邮箱查询用户信息
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    @Override
    public UsersDO getUserByEmail(String email) {
        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 查询用户信息
        return this.lambdaQuery()
                .eq(UsersDO::getEmail, email) // 根据邮箱查询
                .one(); // 返回单个用户对象
    }
}
