package com.digitalcopyright.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;


/**
 * <p>
 *
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
// 指定表名为 users
@TableName("price")
public class PriceDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private BigInteger auctionId;

    private Integer userId;

    private BigInteger workId;

    private BigInteger price;

    private String transactionHash;

    private LocalDateTime createdAt;



}
