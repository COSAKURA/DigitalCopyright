package com.digitalcopyright.model.VO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Sakura
 */
@Data
public class WorkDetailsVO {
    // 作品 ID
    private Integer workId;
    // 标题
    private String title;
    // 描述
    private String description;
    // 作品哈希
    private String hash;
    // 用户地址（从 UsersDO 表中获取）
    private String userAddress;
    // 审核人地址
    private String reviewer;
    // 状态
    private Integer status;
    // 区块链哈希
    private String blockchainHash;
    // 创建时间
    private LocalDateTime createdAt;
    // 是否在拍卖中
    private Boolean isOnAuction;
    // 类型
    private String category;
}
