package com.digitalcopyright.service;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * @author Sakura
 */
@Service
public interface BlockchainService {
    List<Map<String, String>> getAllBlockDetails();

    List<Map<String, String>> getAllTransactionDetails();

    Map<String, Object> traceTransaction(String transactionHash);

}
