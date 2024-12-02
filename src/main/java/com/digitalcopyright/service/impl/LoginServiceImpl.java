package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitalcopyright.mapper.LoginMapper;
import com.digitalcopyright.model.DTO.LoginDTO;
import com.digitalcopyright.service.LoginService;
import org.springframework.stereotype.Service;

/**
 * @author Sakura
 */
@Service
public class LoginServiceImpl extends ServiceImpl<LoginMapper, LoginDTO> implements LoginService {
}
