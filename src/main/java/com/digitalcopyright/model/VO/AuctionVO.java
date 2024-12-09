package com.digitalcopyright.model.VO;

import lombok.Data;

/**
 * @author Sakura
 */
@Data
public class AuctionVO {
    private String title;
    private String description;
    private String auctionId;
    private String startPrice;
    private String currentPrice;
    private Data endTime;
    private Data status;

}
