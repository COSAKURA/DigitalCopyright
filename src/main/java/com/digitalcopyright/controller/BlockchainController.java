package com.digitalcopyright.controller;

import com.digitalcopyright.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sakura
 */
@RestController
@RequestMapping("/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    /**
     * 获取链上所有区块哈希
     *
     * @return 区块哈希列表
     */
    @GetMapping("/allBlockDetails")
    public ResponseEntity<Map<String, Object>> getAllBlockDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, String>> blockDetails = blockchainService.getAllBlockDetails();

            // 实现分页逻辑
            int total = blockDetails.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);

            List<Map<String, String>> paginatedBlockDetails = blockDetails.subList(start, end);

            // 返回分页数据
            Map<String, Object> response = new HashMap<>();
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("data", paginatedBlockDetails);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * 获取链上所有交易哈希
     *
     * @return 交易哈希列表
     */
    @GetMapping("/allTransactionHashes")
    public ResponseEntity<Map<String, Object>> getAllTransactionDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Map<String, String>> transactionDetails = blockchainService.getAllTransactionDetails();

            // 实现分页逻辑
            int total = transactionDetails.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);

            List<Map<String, String>> paginatedTransactionDetails = transactionDetails.subList(start, end);

            // 返回分页数据
            Map<String, Object> response = new HashMap<>();
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("data", paginatedTransactionDetails);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 根据交易哈希溯源
     *
     * @param transactionHash 交易哈希
     * @return 交易详情
     */
    @GetMapping("/traceTransaction")
    public ResponseEntity<?> traceTransaction(@RequestParam String transactionHash) {
        Map<String, Object> transactionDetails = blockchainService.traceTransaction(transactionHash);
        if (transactionDetails.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transactionDetails);
        }
        return ResponseEntity.ok(transactionDetails);
    }
}
