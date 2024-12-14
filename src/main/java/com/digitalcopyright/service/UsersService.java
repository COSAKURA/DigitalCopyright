package com.digitalcopyright.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DTO.LoginDTO;
import com.digitalcopyright.utils.R;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@Service
public interface UsersService extends IService<UsersDO> {

    void bindBlockchainAddress(String email, String blockchainAddress);

    R login(LoginDTO login);

    /**
     * 根据邮箱查询用户信息
     *
     * @param email 用户邮箱
     * @return 用户信息
     */
    UsersDO getUserByEmail(String email);
}
