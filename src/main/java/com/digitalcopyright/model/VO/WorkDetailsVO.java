package com.digitalcopyright.model.VO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkDetailsVO {
    private Integer workId; // 作品 ID
    private String title; // 标题
    private String description; // 描述
    private String hash; // 作品哈希
    private String userAddress; // 用户地址（从 UsersDO 表中获取）
    private String reviewer; // 审核人地址
    private Integer status; // 状态
    private String blockchainHash; // 区块链哈希
    private LocalDateTime createdAt; // 创建时间
    private Boolean isOnAuction; // 是否在拍卖中
    private String category; // 类型
}
