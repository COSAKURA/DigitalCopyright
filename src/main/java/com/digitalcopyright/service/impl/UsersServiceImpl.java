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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, UsersDO> implements UsersService {

    @Resource
    private UsersMapper usersMapper;

    @Override
    public R login(LoginDTO login) {
        // 获取邮箱和密码并去除空格
        String email = login.getEmail().trim();
        String password = login.getPassword().trim();

        // 查询用户信息
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null) {
            return R.error(BizCodeEnum.NO_SUCHUSER.getCode(), BizCodeEnum.NO_SUCHUSER.getMsg());
        }

        // 验证密码
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            return R.error(BizCodeEnum.BAD_PUTDATA.getCode(), BizCodeEnum.BAD_PUTDATA.getMsg());
        }

        // 生成登录 Token
        String token = JwtTokenUtil.generateToken(user);

        // 返回成功结果
        return R.ok(BizCodeEnum.SUCCESSFUL.getMsg()).put("loginToken", token);
    }

    @Override
    public void bindBlockchainAddress(String email, String blockchainAddress) {
        // 根据邮箱查找用户
        QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UsersDO user = usersMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 绑定区块链地址
        user.setBlockchainAddress(blockchainAddress);
        int updateResult = usersMapper.updateById(user);

        if (updateResult <= 0) {
            throw new RuntimeException("区块链地址绑定失败");
        }
    }

}
