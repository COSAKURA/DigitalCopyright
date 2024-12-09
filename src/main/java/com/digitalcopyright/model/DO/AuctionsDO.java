package com.digitalcopyright.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.web3j.abi.datatypes.Int;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;

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
@TableName("auctions") // 指定表名为 auctions
public class AuctionsDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private BigInteger workId;

    private Integer sellerId;

    private BigInteger startPrice;

    private BigInteger currentPrice;

    private Integer buyerId;

    private LocalDateTime endTime;

    /**
     * 0 - active, 1 - ended
     */
    private Integer status;

    private String transactionHash;

    private BigInteger auctionId;


}
