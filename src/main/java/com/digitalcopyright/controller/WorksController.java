package com.digitalcopyright.controller;

import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.model.DTO.RegisterWorkDTO;
import com.digitalcopyright.model.VO.WorkDetailsVO;
import com.digitalcopyright.service.WorksService;
import com.digitalcopyright.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("/work")
@Slf4j
public class WorksController {

    @Autowired
    private WorksService worksService;

    /**
     * 用户上传作品并保存，同时进行作品上链。
     *
     * @param registerWorkDTO 注册作品的DTO，包括邮箱、标题、描述
     * @return 操作结果
     */
    @PostMapping("/registerWork")
    public R registerWork(@ModelAttribute RegisterWorkDTO registerWorkDTO) {
        try {
            worksService.registerWork(registerWorkDTO);
            return R.ok("作品注册成功");
        } catch (IllegalArgumentException e) {
            log.error("参数校验异常: {}", e.getMessage());
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("作品注册失败: {}", e.getMessage(), e);
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
        }
    }

    /**
     * 获取作品详细信息
     *
     * @param workId 作品 ID
     * @return 作品详细信息
     */
    @GetMapping("/workDetails")
    public R getWorkDetails(@RequestParam BigInteger workId) {
        try {
            WorkDetailsVO workDetails = worksService.getWorkDetails(workId);
            return R.ok("获取作品详情成功").put("data", workDetails);
        } catch (IllegalArgumentException e) {
            log.error("参数校验异常: {}", e.getMessage());
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取作品详情失败: {}", e.getMessage(), e);
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
        }
    }

    /**
     * 用户申请版权
     *
     * @param workId 作品ID
     * @return 操作结果
     */
    @PostMapping("/applyCopyright")
    public R applyCopyright(
            @RequestParam String email ,
            @RequestParam BigInteger workId) {
        try {
            worksService.applyCopyright(email, workId );
            return R.ok("申请版权成功，版权已自动上链");
        } catch (IllegalArgumentException e) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "申请版权失败");
        }
    }



}
