package com.digitalcopyright.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Sakura
 */
@Service
public interface BlockchainService {
    Map<String, Object> getAllBlockDetails();

    List<Map<String, String>> getAllTransactionDetails();

    Map<String, Object> traceTransaction(String transactionHash);

}
