package com.digitalcopyright.service.impl;

import com.digitalcopyright.service.BlockchainService;
import jakarta.annotation.Resource;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;

import org.fisco.bcos.sdk.client.protocol.response.*;

import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Sakura
 */
@Service
public class BlockchainServiceImpl implements BlockchainService {

    @Resource
    private Client client; // 区块链客户端，用于与区块链交互

    /**
     * 获取链上所有区块的哈希和统计信息
     * @return 包括区块哈希列表、节点个数、已部署合约数、区块数量和交易数量
     */
    @Override
    public Map<String, Object> getAllBlockDetails() {
        // 初始化返回结果
        Map<String, Object> response = new HashMap<>();
        // 用于存储每个区块的详细信息
        List<Map<String, String>> blockDetailsList = new ArrayList<>();

        try {
            // 获取最新区块号
            BigInteger latestBlockNumber = client.getBlockNumber().getBlockNumber();
            // 格式化时间戳
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 遍历所有区块，获取区块哈希和时间戳
            for (BigInteger i = BigInteger.ZERO; i.compareTo(latestBlockNumber) <= 0; i = i.add(BigInteger.ONE)) {
                // 获取区块头信息（不包含完整交易信息，提升查询性能）
                BcosBlock block = client.getBlockByNumber(i, false);
                if (block != null && block.getResult() != null) {
                    // 存储单个区块的详细信息
                    Map<String, String> blockDetail = new HashMap<>();
                    blockDetail.put("blockHash", block.getResult().getHash()); // 区块哈希
                    blockDetail.put("blockNumber", i.toString()); // 区块号

                    // 格式化时间戳（区块时间以十六进制表示，需要转换为毫秒）
                    String timestampHex = block.getResult().getTimestamp();
                    long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                    blockDetail.put("timestamp", sdf.format(new java.util.Date(timestampMillis))); // 时间戳

                    // 添加到区块详情列表中
                    blockDetailsList.add(blockDetail);
                }
            }

            // 统计信息
            Map<String, Object> statistics = new HashMap<>();

            // 获取节点列表
            try {
                List<String> peers = client.getGroupPeers().getResult(); // 获取所有组的节点
                statistics.put("nodeList", peers); // 节点列表
                statistics.put("nodeCount", peers.size()); // 节点数量
            } catch (Exception e) {
                // 如果获取节点信息失败，添加默认值
                statistics.put("nodeCount", "Unable to fetch");
            }

            // 区块数量
            statistics.put("blockCount", latestBlockNumber.longValue());

            // 获取总交易统计信息
            TotalTransactionCount.TransactionCountInfo transactionCount = client.getTotalTransactionCount().getTotalTransactionCount();
            // 将交易总数和失败交易数从十六进制转换为十进制
            BigInteger txSumDecimal = new BigInteger(transactionCount.getTxSum().substring(2), 16);
            BigInteger failedTxSumDecimal = new BigInteger(transactionCount.getFailedTxSum().substring(2), 16);

            // 添加交易统计数据
            statistics.put("txSum", txSumDecimal); // 总交易数
            statistics.put("failedTxSum", failedTxSumDecimal); // 失败交易数

            // 组装结果
            response.put("blockDetails", blockDetailsList); // 区块详情列表
            response.put("statistics", statistics); // 区块链统计信息

        } catch (Exception e) {
            // 捕获异常并抛出带有详细错误信息的 RuntimeException
            throw new RuntimeException("Error fetching blockchain details: " + e.getMessage());
        }

        // 返回结果
        return response;
    }


    /**
     * 获取链上所有区块的交易哈希
     * @return 所有交易哈希的列表，每个交易包含哈希值、发起地址、接收地址、区块号和时间戳
     */
    @Override
    public List<Map<String, String>> getAllTransactionDetails() {
        // 获取最新区块号
        BigInteger latestBlockNumber = client.getBlockNumber().getBlockNumber();
        // 用于存储所有交易详细信息的列表
        List<Map<String, String>> transactionDetailsList = new ArrayList<>();
        // 格式化区块时间戳的格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 遍历区块，从 0 到最新区块号
        for (BigInteger i = BigInteger.ZERO; i.compareTo(latestBlockNumber) <= 0; i = i.add(BigInteger.ONE)) {
            // 将当前区块号赋值给新的变量（确保线程安全）
            BigInteger blockNumber = i;

            // 获取完整的区块信息，包括交易列表
            BcosBlock block = client.getBlockByNumber(blockNumber, true);
            if (block != null && block.getResult() != null) {
                // 获取区块时间戳（以十六进制表示）并格式化为人类可读的时间
                String timestampHex = block.getResult().getTimestamp();
                long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                String formattedTimestamp = sdf.format(new java.util.Date(timestampMillis));

                // 遍历区块中的交易列表
                block.getResult().getTransactions().forEach(transactionResult -> {
                    if (transactionResult instanceof BcosBlock.TransactionObject) {
                        // 如果交易对象是 TransactionObject 类型，提取交易信息
                        BcosBlock.TransactionObject transactionObject = (BcosBlock.TransactionObject) transactionResult;

                        // 创建一个 Map 用于存储交易的详细信息
                        Map<String, String> transactionDetail = new HashMap<>();
                        transactionDetail.put("transactionHash", transactionObject.getHash()); // 交易哈希
                        transactionDetail.put("from", transactionObject.getFrom());           // 交易发起地址
                        transactionDetail.put("to", transactionObject.getTo());               // 交易接收地址
                        transactionDetail.put("timestamp", formattedTimestamp);               // 交易时间戳
                        transactionDetail.put("blockNumber", blockNumber.toString());         // 所属区块号

                        // 将当前交易信息添加到结果列表
                        transactionDetailsList.add(transactionDetail);
                    } else {
                        // 如果交易对象类型与预期不符，打印错误日志
                        System.out.println("Unexpected transaction result type: " + transactionResult.getClass());
                    }
                });
            }
        }

        // 返回所有交易的详细信息
        return transactionDetailsList;
    }



    @Override
    public Map<String, Object> traceTransaction(String transactionHash) {
        // 初始化结果 Map，用于存储交易详细信息
        Map<String, Object> transactionDetails = new HashMap<>();
        try {
            // 1. 获取交易详细信息
            BcosTransaction transactionResponse = client.getTransactionByHash(transactionHash);

            // 检查交易是否存在
            if (transactionResponse != null && transactionResponse.getResult() != null) {
                JsonTransactionResponse transaction = transactionResponse.getResult();

                // 将交易基本信息添加到结果 Map 中
                transactionDetails.put("transactionHash", transaction.getHash()); // 交易哈希
                transactionDetails.put("from", transaction.getFrom());           // 交易发起地址
                transactionDetails.put("to", transaction.getTo());               // 交易接收地址
                transactionDetails.put("nonce", transaction.getNonce());         // 随机数
                transactionDetails.put("blockHash", transaction.getBlockHash()); // 所属区块哈希
                transactionDetails.put("blockNumber", transaction.getBlockNumber()); // 所属区块号
                transactionDetails.put("input", transaction.getInput());         // 交易输入数据

                // 2. 获取交易回执
                BcosTransactionReceipt receiptResponse = client.getTransactionReceipt(transactionHash);
                if (receiptResponse != null && receiptResponse.getResult() != null) {
                    org.fisco.bcos.sdk.model.TransactionReceipt receipt = receiptResponse.getResult();

                    // 添加交易回执中的详细信息
                    transactionDetails.put("gasUsed", receipt.getGasUsed());       // 消耗的 Gas
                    transactionDetails.put("status", receipt.getStatus());         // 交易状态（0x0 表示成功）
                    transactionDetails.put("contractAddress", receipt.getContractAddress()); // 合约地址

                    // 提取消息（如果没有消息，根据状态提供默认值）
                    String message = receipt.getMessage();
                    if (message == null) {
                        message = receipt.getStatus().equals("0x0") ? "Transaction succeeded" : "Transaction failed";
                    }
                    transactionDetails.put("Message", message);

                    // 解析事件日志
                    List<TransactionReceipt.Logs> logs = receipt.getLogs();
                    if (logs != null && !logs.isEmpty()) {
                        // 遍历并处理每条日志
                        logs.forEach(log -> {
                            // 这里可以实现具体的事件日志解码逻辑
                            System.out.println("Log: " + log);
                        });
                    }
                }

                // 3. 获取区块时间戳并格式化为人类可读的时间
                BcosBlock blockResponse = client.getBlockByHash(transaction.getBlockHash(), false);
                if (blockResponse != null && blockResponse.getResult() != null) {
                    String timestampHex = blockResponse.getResult().getTimestamp();
                    // 将十六进制时间戳转换为毫秒值
                    long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                    // 格式化时间戳
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedTimestamp = dateFormat.format(new Date(timestampMillis));
                    transactionDetails.put("timestamp", formattedTimestamp); // 添加时间戳
                }
            } else {
                // 如果未找到交易，返回错误消息
                transactionDetails.put("error", "Transaction not found for hash: " + transactionHash);
            }
        } catch (Exception e) {
            // 捕获异常并添加错误信息
            transactionDetails.put("error", "Error tracing transaction: " + e.getMessage());
        }

        // 返回交易详细信息
        return transactionDetails;
    }



}
