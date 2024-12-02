package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitalcopyright.mapper.RegisterMapper;
import com.digitalcopyright.model.DTO.RegisterDTO;
import com.digitalcopyright.service.RegisterService;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl extends ServiceImpl<RegisterMapper, RegisterDTO> implements RegisterService {
}
