package com.digitalcopyright.service;


import com.digitalcopyright.model.DO.AuctionsDO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author sakura
 * @since 2024-11-27
 */
@Service
public interface AuctionsService {

    void startAuction(String email, BigInteger workId, BigInteger startPrice, long duration,String privateKey);

    void placeBid(String email, BigInteger auctionId, BigInteger bidAmount, String privateKey);

    void endAuction(String email, BigInteger auctionId, String privateKey);

    List<Map<String, Object>> getAllAuctions();
}
