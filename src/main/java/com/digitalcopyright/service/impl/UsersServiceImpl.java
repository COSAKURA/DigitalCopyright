package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.UsersService;
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

}
