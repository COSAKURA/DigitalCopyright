package com.digitalcopyright.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.digitalcopyright.model.DTO.LoginDTO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@Mapper
public interface LoginMapper extends BaseMapper<LoginDTO> {
}
