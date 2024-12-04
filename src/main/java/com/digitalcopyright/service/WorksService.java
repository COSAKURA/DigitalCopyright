package com.digitalcopyright.service;

import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.model.DTO.RegisterWorkDTO;
import com.digitalcopyright.model.VO.WorkDetailsVO;

import java.math.BigInteger;
import java.util.Map;

public interface WorksService {
    /**
     * 注册作品，包括文件保存、用户绑定、作品入库
     *
     * @param registerWorkDTO 注册作品的DTO
     */
    void registerWork(RegisterWorkDTO registerWorkDTO);


    /**
     * 查询链上作品
     *
     * @param workId 注册链上作品的id
     * @return 作品信息
     */
    WorkDetailsVO getWorkDetails(BigInteger workId);

    void applyCopyright(String email, BigInteger workId);
}
