package com.digitalcopyright.controller;


import com.digitalcopyright.common.BizCodeEnum;

import com.digitalcopyright.service.AuctionsService;
import com.digitalcopyright.utils.R;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@RestController
@RequestMapping("/auctions")
public class AuctionsController {

    @Resource
    private AuctionsService auctionService;

    /**
     * 用户发起版权拍卖
     *
     * @param email 用户邮箱
     * @param workId 作品ID
     * @param startPrice 起始价格
     * @param duration 拍卖持续时间（秒）
     * @return 操作结果
     */
    @PostMapping("/startAuction")
    public R startAuction(
            @RequestParam String email,
            @RequestParam BigInteger workId,
            @RequestParam BigInteger startPrice,
            @RequestParam long duration,
            @RequestParam("privateKey") String privateKey) {
        try {
            auctionService.startAuction(email, workId, startPrice, duration,privateKey);
            return R.ok("拍卖成功，作品已上链");
        } catch (IllegalArgumentException e) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "拍卖失败");
        }
    }

    // 用户竞价接口
    @PostMapping("/placeBid")
    public R placeBid(
            @RequestParam String email,
            @RequestParam BigInteger auctionId,
            @RequestParam BigInteger bidAmount,
            @RequestParam String privateKey) {
        try {

            auctionService.placeBid(email, auctionId, bidAmount, privateKey);
            return R.ok("竞拍成功！");
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), e.getMessage());
        }
    }

    /**
     * 结束拍卖接口
     * @param auctionId 拍卖ID
     * @param privateKey 用户私钥
     * @return 响应结果
     */
    @PostMapping("/endAuction")
    public R endAuction(
            @RequestParam("auctionId") BigInteger auctionId,
            @RequestParam("privateKey") String privateKey,
            @RequestParam("email") String email) {
        try {
            // 调用服务层方法结束拍卖
            auctionService.endAuction(email, auctionId, privateKey);
            return R.ok("拍卖已成功结束");
        } catch (IllegalArgumentException e) {
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "结束拍卖失败：" + e.getMessage());
        } catch (Exception e) {
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "系统错误，请稍后再试");
        }
    }

    /**
     * 查询所有参与拍卖的作品信息
     *
     * @return 竞拍数据
     */
    @GetMapping("/getAllAuctions")
    public R getAllAuctions() {
        try {
            // 调用 Service 层方法获取所有拍卖数据
            List<Map<String, Object>> auctions = auctionService.getAllAuctions();

            // 如果没有拍卖数据，返回提示信息
            if (auctions.isEmpty()) {
                return R.ok().put("message", "没有找到任何拍卖数据");
            }
            // 返回成功并带有拍卖数据
            return R.ok().put("auctions", auctions);
        } catch (Exception e) {
            return R.error("获取拍卖数据失败：" + e.getMessage());
        }
    }


    /**
     * 根据拍卖 ID 获取拍卖信息
     * @param workId 拍卖 ID
     * @return  拍卖信息
     */
    @GetMapping("/getAuctionById")
    public R getAuctionById(@RequestParam Integer workId) {
        try {
            // 调用 Service 层方法根据拍卖 ID 获取数据
            Map<String, Object> auction = auctionService.getAuctionById(workId);

            // 如果找不到对应的拍卖数据，返回提示信息
            if (auction == null) {
                return R.ok().put("message", "未找到对应的拍卖数据");
            }
            // 返回成功并带有拍卖数据
            return R.ok().put("auction", auction);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return R.error("获取拍卖数据失败：" + e.getMessage());
        }
    }
}

