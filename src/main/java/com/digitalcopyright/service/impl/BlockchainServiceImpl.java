package com.digitalcopyright.service.impl;

import com.digitalcopyright.service.BlockchainService;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;

import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransaction;
import org.fisco.bcos.sdk.client.protocol.response.BcosTransactionReceipt;

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

    @Autowired
    private Client client;

    /**
     * 获取链上所有区块的哈希
     *
     * @return 所有区块哈希的列表
     */
    @Override
    public List<Map<String, String>> getAllBlockDetails() {
        BigInteger latestBlockNumber = client.getBlockNumber().getBlockNumber();
        List<Map<String, String>> blockDetailsList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (BigInteger i = BigInteger.ZERO; i.compareTo(latestBlockNumber) <= 0; i = i.add(BigInteger.ONE)) {
            BcosBlock block = client.getBlockByNumber(i, false); // 只获取区块头
            if (block != null && block.getResult() != null) {
                Map<String, String> blockDetail = new HashMap<>();
                blockDetail.put("blockHash", block.getResult().getHash());
                blockDetail.put("blockNumber", i.toString());

                // 获取并格式化时间戳
                String timestampHex = block.getResult().getTimestamp();
                long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                blockDetail.put("timestamp", sdf.format(new java.util.Date(timestampMillis)));

                blockDetailsList.add(blockDetail);
            }
        }

        return blockDetailsList;
    }


    /**
     * 获取链上所有区块的交易哈希
     *
     * @return 所有交易哈希的列表
     */
    @Override
    public List<Map<String, String>> getAllTransactionDetails() {
        BigInteger latestBlockNumber = client.getBlockNumber().getBlockNumber();
        List<Map<String, String>> transactionDetailsList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (BigInteger i = BigInteger.ZERO; i.compareTo(latestBlockNumber) <= 0; i = i.add(BigInteger.ONE)) {
            BigInteger blockNumber = i; // 将 i 赋值给一个新的变量
            BcosBlock block = client.getBlockByNumber(blockNumber, true); // 获取完整区块，包括交易列表
            if (block != null && block.getResult() != null) {
                String timestampHex = block.getResult().getTimestamp();
                long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                String formattedTimestamp = sdf.format(new java.util.Date(timestampMillis));

                // 遍历交易列表
                block.getResult().getTransactions().forEach(transactionResult -> {
                    if (transactionResult instanceof BcosBlock.TransactionObject) {
                        BcosBlock.TransactionObject transactionObject = (BcosBlock.TransactionObject) transactionResult;

                        Map<String, String> transactionDetail = new HashMap<>();
                        transactionDetail.put("transactionHash", transactionObject.getHash());
                        transactionDetail.put("from", transactionObject.getFrom());
                        transactionDetail.put("to", transactionObject.getTo());
                        transactionDetail.put("timestamp", formattedTimestamp);
                        transactionDetail.put("blockNumber", blockNumber.toString()); // 使用新的变量

                        transactionDetailsList.add(transactionDetail);
                    } else {
                        System.out.println("Unexpected transaction result type: " + transactionResult.getClass());
                    }
                });
            }
        }

        return transactionDetailsList;
    }



    @Override
    public Map<String, Object> traceTransaction(String transactionHash) {
        Map<String, Object> transactionDetails = new HashMap<>();
        try {
            // 获取交易详细信息
            BcosTransaction transactionResponse = client.getTransactionByHash(transactionHash);

            if (transactionResponse != null && transactionResponse.getResult() != null) {
                JsonTransactionResponse transaction = transactionResponse.getResult();
                transactionDetails.put("transactionHash", transaction.getHash());
                transactionDetails.put("from", transaction.getFrom());
                transactionDetails.put("to", transaction.getTo());
                transactionDetails.put("nonce", transaction.getNonce());
                transactionDetails.put("blockHash", transaction.getBlockHash());
                transactionDetails.put("blockNumber", transaction.getBlockNumber());
                transactionDetails.put("input", transaction.getInput());

                // 获取交易回执
                BcosTransactionReceipt receiptResponse = client.getTransactionReceipt(transactionHash);
                if (receiptResponse != null && receiptResponse.getResult() != null) {
                    org.fisco.bcos.sdk.model.TransactionReceipt receipt = receiptResponse.getResult();
                    transactionDetails.put("gasUsed", receipt.getGasUsed());
                    transactionDetails.put("status", receipt.getStatus());
                    transactionDetails.put("contractAddress", receipt.getContractAddress());

                    // 提取消息或使用默认值
                    String message = receipt.getMessage();
                    if (message == null) {
                        message = receipt.getStatus().equals("0x0") ? "Transaction succeeded" : "Transaction failed";
                    }
                    transactionDetails.put("Message", message);

                    // 解析事件日志
                    List<TransactionReceipt.Logs> logs = receipt.getLogs();
                    if (logs != null && !logs.isEmpty()) {
                        logs.forEach(log -> {
                            // 解码日志（需具体事件 ABI 解码实现）
                            System.out.println("Log: " + log);
                        });
                    }
                }
                // 获取区块时间戳并格式化为 yyyy-MM-dd HH:mm:ss
                BcosBlock blockResponse = client.getBlockByHash(transaction.getBlockHash(), false);
                if (blockResponse != null && blockResponse.getResult() != null) {
                    String timestampHex = blockResponse.getResult().getTimestamp();
                    // 将十六进制时间戳转换为毫秒值
                    long timestampMillis = new BigInteger(timestampHex.substring(2), 16).longValue();
                    // 格式化时间戳
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedTimestamp = dateFormat.format(new Date(timestampMillis));
                    transactionDetails.put("timestamp", formattedTimestamp);
                }
            } else {
                transactionDetails.put("error", "Transaction not found for hash: " + transactionHash);
            }
        } catch (Exception e) {
            transactionDetails.put("error", "Error tracing transaction: " + e.getMessage());
        }
        return transactionDetails;
    }



}
