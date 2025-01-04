package com.digitalcopyright.controller;

import com.digitalcopyright.common.BizCodeEnum;
import com.digitalcopyright.model.VO.WorkDetailsVO;
import com.digitalcopyright.service.WorksService;
import com.digitalcopyright.utils.R;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 作品管理控制器
 * 提供作品相关的操作接口，包括作品上传、作品详情查询、版权申请以及用户作品列表获取。
 * 功能：
 * 1. 用户上传作品并进行链上注册。
 * 2. 获取作品的详细信息。
 * 3. 用户申请作品版权并上链。
 * 4. 获取用户所有作品列表。
 * 作者：Sakura
 */
@RestController
@RequestMapping("/work")
@Slf4j
public class WorksController {

    @Resource
    private WorksService worksService; // 注入作品服务，处理作品相关业务逻辑

    /**
     * 用户上传作品并保存，同时进行作品上链
     * @param file 上传的作品文件
     * @param title 作品标题
     * @param description 作品描述
     * @param privateKey 用户私钥，用于链上操作
     * @param email 用户邮箱，用于标识上传者
     * @return 操作结果
     */
    @PostMapping(value = "/registerWork", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R uploadWork(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("privateKey") String privateKey,
            @RequestParam("email") String email
    ) {
        try {
            // 调用服务层逻辑完成作品注册及链上操作
            worksService.registerWork(file, title, description, privateKey, email);
            return R.ok("作品上传成功");
        } catch (IllegalArgumentException e) {
            return R.error(400, e.getMessage());
        } catch (Exception e) {
            return R.error(500, "作品上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取作品详细信息
     * 此接口用于查询指定作品的详细信息，包括标题、描述、版权状态等。
     * @param workId 作品 ID
     * @return 作品详细信息
     */
    @GetMapping("/workDetails")
    public R getWorkDetails(@RequestParam BigInteger workId) {
        try {
            // 调用服务层获取作品详情
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
     * 此接口用于用户为自己的作品申请版权，并完成链上注册操作。
     * @param email 用户邮箱
     * @param workId 作品 ID
     * @return 操作结果
     */
    @PostMapping("/applyCopyright")
    public R applyCopyright(
            @RequestParam String email,
            @RequestParam BigInteger workId) {
        try {
            // 调用服务层逻辑完成版权申请
            worksService.applyCopyright(email, workId);
            return R.ok("申请版权成功，版权已自动上链");
        } catch (IllegalArgumentException e) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "申请版权失败");
        }
    }

    /**
     * 获取用户所有作品
     * 此接口用于查询指定用户的所有作品列表，包括作品的基本信息。
     * @param email 用户邮箱
     * @return 用户作品列表
     */
    @GetMapping("/userWorksAll")
    public R getUserWorks(@RequestParam String email) {
        try {
            // 调用服务层获取作品列表
            List<Map<String, Object>> worksData = worksService.getUserWorksByEmail(email);
            return R.ok("获取用户作品列表成功").put("data", worksData);
        } catch (IllegalArgumentException e) {
            log.error("参数校验异常: {}", e.getMessage());
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取用户作品列表失败: {}", e.getMessage(), e);
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
        }
    }
}
