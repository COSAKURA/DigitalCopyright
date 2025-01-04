package com.digitalcopyright.controller;

import com.digitalcopyright.service.BlockchainService;
import jakarta.annotation.Resource;
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

    @Resource
    private BlockchainService blockchainService; // 注入区块链服务，用于与区块链交互

    /**
     * 获取链上所有区块哈希信息
     * 此接口用于从区块链服务中获取所有区块的详细信息，包括区块哈希、统计数据等。
     * 支持分页返回，前端可以通过 `page` 和 `size` 参数进行分页控制。
     * @param page 当前页码，默认为 0
     * @param size 每页记录数，默认为 10
     * @return 分页后的区块哈希列表及统计信息
     */
    @GetMapping("/allBlockDetails")
    public ResponseEntity<Map<String, Object>> getAllBlockDetails(
            @RequestParam(defaultValue = "0") int page, // 接收分页的页码（默认为第 0 页）
            @RequestParam(defaultValue = "10") int size // 接收分页的大小（默认为 10 条记录）
    ) {
        try {
            // 从区块链服务中获取所有区块详情
            Map<String, Object> blockDetails = blockchainService.getAllBlockDetails();

            // 从 blockDetails 中提取区块列表
            @SuppressWarnings("unchecked")
            List<Map<String, String>> blockDetailsList = (List<Map<String, String>>) blockDetails.get("blockDetails");

            // 获取区块总数
            int total = blockDetailsList.size();

            // 计算分页的起始位置和结束位置
            int start = Math.min(page * size, total); // 起始位置（如果超过总数，则取最大索引）
            int end = Math.min(start + size, total); // 结束位置（如果超过总数，则取最大索引）

            // 获取分页后的区块详情列表
            List<Map<String, String>> paginatedBlockDetails = blockDetailsList.subList(start, end);

            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("total", total); // 区块总数
            response.put("page", page); // 当前页码
            response.put("size", size); // 每页记录数
            response.put("data", paginatedBlockDetails); // 分页后的区块数据

            // 添加统计信息（如区块总数、交易总数等）
            response.put("statistics", blockDetails.get("statistics"));

            // 返回分页响应
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * 获取链上所有交易哈希
     * 此接口用于从区块链获取所有交易的详细信息，包括交易哈希、区块号、时间戳等。
     * 支持分页返回，前端可以通过 `page` 和 `size` 参数控制分页。
     * @param page 当前页码，默认为 0
     * @param size 每页记录数，默认为 5
     * @return 分页后的交易哈希列表
     */
    @GetMapping("/allTransactionHashes")
    public ResponseEntity<Map<String, Object>> getAllTransactionDetails(
            @RequestParam(defaultValue = "0") int page, // 分页的页码，默认为第 0 页
            @RequestParam(defaultValue = "5") int size // 分页的大小，默认为每页 5 条记录
    ) {
        try {
            // 从区块链服务中获取所有交易详情
            List<Map<String, String>> transactionDetails = blockchainService.getAllTransactionDetails();

            // 获取总交易数
            int total = transactionDetails.size();

            // 计算分页起始位置和结束位置
            int start = Math.min(page * size, total); // 起始位置（如果超出总数，则取最大索引）
            int end = Math.min(start + size, total); // 结束位置（如果超出总数，则取最大索引）

            // 获取分页后的交易详情列表
            List<Map<String, String>> paginatedTransactionDetails = transactionDetails.subList(start, end);

            // 构建分页响应
            Map<String, Object> response = new HashMap<>();
            response.put("total", total); // 交易总数
            response.put("page", page); // 当前页码
            response.put("size", size); // 每页记录数
            response.put("data", paginatedTransactionDetails); // 分页后的交易数据

            // 返回分页响应
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 捕获异常并返回错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }



    /**
     * 根据交易哈希溯源
     * 此接口通过交易哈希查询对应的链上交易详情，用户可以使用该接口获取交易的具体信息。
     * 如果交易不存在或查询失败，将返回错误提示。
     * @param transactionHash 交易哈希，唯一标识链上的一笔交易
     * @return 交易详情或错误信息
     */
    @GetMapping("/traceTransaction")
    public ResponseEntity<?> traceTransaction(@RequestParam String transactionHash) {
        // 调用区块链服务，通过交易哈希获取交易详情
        Map<String, Object> transactionDetails = blockchainService.traceTransaction(transactionHash);

        // 判断是否包含错误信息
        if (transactionDetails.containsKey("error")) {
            // 如果包含错误，返回 HTTP 400 (BAD REQUEST) 和错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(transactionDetails);
        }

        // 成功返回交易详情，HTTP 状态码为 200 (OK)
        return ResponseEntity.ok(transactionDetails);
    }


}
