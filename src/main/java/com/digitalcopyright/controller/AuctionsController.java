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

    // 注入拍卖服务，用于处理拍卖相关的业务逻辑
    @Resource
    private AuctionsService auctionService;

    /**
     * 用户发起版权拍卖
     * @param email 用户邮箱，用于标识发起拍卖的用户
     * @param workId 作品ID，表示需要发起拍卖的作品的唯一标识
     * @param startPrice 起始价格，拍卖开始的最低价格
     * @param duration 拍卖持续时间（秒），定义拍卖的有效期
     * @param privateKey 用户的私钥，用于签署链上交易
     * @return 操作结果，返回操作成功或失败的响应
     */
    @PostMapping("/startAuction")
    public R startAuction(
            @RequestParam String email, // 接收用户邮箱
            @RequestParam BigInteger workId, // 接收作品ID
            @RequestParam BigInteger startPrice, // 接收起始价格
            @RequestParam long duration, // 接收拍卖持续时间（单位：秒）
            @RequestParam("privateKey") String privateKey // 接收用户私钥
    ) {
        try {
            // 调用拍卖服务发起拍卖
            auctionService.startAuction(email, workId, startPrice, duration, privateKey);

            // 返回成功响应，提示用户拍卖已成功发起
            return R.ok("拍卖成功，作品已上链");
        } catch (IllegalArgumentException e) {
            // 捕获参数校验失败异常，返回参数校验错误信息
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (Exception e) {
            // 捕获未知异常，返回拍卖失败的错误提示
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "拍卖失败");
        }
    }


    /**
     * 竞拍接口
     * 此接口用于用户参与竞拍，通过提供拍卖ID、出价金额和用户的区块链私钥完成竞拍。
     * @param email 用户邮箱，用于标识竞拍的用户
     * @param auctionId 拍卖ID，指定用户参与的具体拍卖活动
     * @param bidAmount 出价金额，用户竞拍时所出的价格，必须高于当前最高出价
     * @param privateKey 用户私钥，用于链上签名验证，确保竞拍的合法性和安全性
     * @return 响应结果，包括成功或失败信息
     */
    @PostMapping("/placeBid")
    public R placeBid(
            @RequestParam String email, // 接收用户邮箱
            @RequestParam BigInteger auctionId, // 接收拍卖ID
            @RequestParam BigInteger bidAmount, // 接收出价金额
            @RequestParam String privateKey // 接收用户的区块链私钥
    ) {
        try {
            // 调用拍卖服务进行出价操作
            auctionService.placeBid(email, auctionId, bidAmount, privateKey);
            // 如果成功，则返回成功响应信息
            return R.ok("竞拍成功！");
        } catch (Exception e) {
            // 捕获所有未知异常，返回错误信息
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), e.getMessage());
        }
    }


    /**
     * 结束拍卖接口
     * 此接口用于拍卖发起人或授权用户结束拍卖，调用该接口后拍卖状态将被更新为结束，并触发相关链上操作。
     * @param auctionId 拍卖ID，指定需要结束的拍卖活动
     * @param privateKey 用户私钥，用于验证用户身份并执行链上操作
     * @param email 用户邮箱，用于标识当前操作的用户
     * @return 响应结果，包括成功或失败的提示信息
     */
    @PostMapping("/endAuction")
    public R endAuction(
            @RequestParam("auctionId") BigInteger auctionId, // 接收拍卖ID
            @RequestParam("privateKey") String privateKey, // 接收用户的区块链私钥
            @RequestParam("email") String email // 接收用户邮箱
    ) {
        try {
            // 调用服务层方法结束拍卖
            auctionService.endAuction(email, auctionId, privateKey);

            // 如果成功，返回成功响应信息
            return R.ok("拍卖已成功结束");
        } catch (IllegalArgumentException e) {
            // 捕获参数校验失败异常，例如无效的拍卖ID或私钥格式错误
            return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            // 捕获运行时异常，返回结束拍卖失败的具体原因
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "结束拍卖失败：" + e.getMessage());
        } catch (Exception e) {
            // 捕获其他未知异常，返回通用的系统错误提示
            return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "系统错误，请稍后再试");
        }
    }


    /**
     * 查询所有参与拍卖的作品信息
     * 此接口用于获取所有正在进行的拍卖数据，包括拍卖作品的信息、当前价格、起始价格等。
     * 前端可以通过调用该接口展示拍卖列表。
     * @return 包含所有拍卖数据的响应结果
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
            // 捕获所有异常，返回错误信息
            return R.error("获取拍卖数据失败：" + e.getMessage());
        }
    }



    /**
     * 根据拍卖 ID 获取拍卖信息
     * 此接口用于根据作品 ID（workId）获取拍卖的详细信息，包括拍卖作品的基本信息、
     * 当前竞拍状态和用户相关的额外信息（如当前用户是否为拍卖发起人）。
     * @param workId 作品 ID，用于标识拍卖活动
     * @param currentUserEmail 当前用户的邮箱，用于判断用户与拍卖的关系
     * @return 包含拍卖详细信息的响应结果
     */
    @GetMapping("/getAuctionById")
    public R getAuctionById(
            @RequestParam Integer workId, // 接收作品 ID（workId）
            @RequestParam String currentUserEmail // 接收当前用户的邮箱
    ) {
        try {
            // 调用 Service 层方法，根据作品 ID 和用户邮箱获取拍卖数据
            Map<String, Object> auction = auctionService.getAuctionById(workId, currentUserEmail);

            // 如果找不到对应的拍卖数据，返回提示信息
            if (auction == null) {
                return R.ok().put("message", "未找到对应的拍卖数据");
            }

            // 返回成功，并附带拍卖数据
            return R.ok().put("auction", auction);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return R.error("获取拍卖数据失败：" + e.getMessage());
        }
    }

}

