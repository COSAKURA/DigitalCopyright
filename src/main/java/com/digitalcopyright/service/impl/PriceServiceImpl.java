package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.digitalcopyright.mapper.PriceMapper;
import com.digitalcopyright.model.DO.PriceDO;
import com.digitalcopyright.service.PriceService;
import org.springframework.stereotype.Service;


@Service
public class PriceServiceImpl extends ServiceImpl<PriceMapper, PriceDO> implements PriceService {
}
