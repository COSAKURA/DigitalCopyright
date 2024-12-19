package com.digitalcopyright.model.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
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
@TableName("works") // 指定表名为 works
@AllArgsConstructor
@NoArgsConstructor
public class WorksDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer workId;

    private String title;

    private String description;

    private String imgUrl;

    private String hash;

    private String blockchainHash;
    /**
     * 0 - pending, 1 - approved
     */
    private Integer status;

    private String digitalCopyrightId;

    private String transactionHash;

    private String category;  // 作品类型

    private Boolean isOnAuction; // 是否在拍卖

    private LocalDateTime createdAt;


}
