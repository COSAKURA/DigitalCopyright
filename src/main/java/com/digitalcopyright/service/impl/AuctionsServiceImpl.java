package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.AuctionsMapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.mapper.WorksMapper;
import com.digitalcopyright.model.DO.AuctionsDO;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.service.AuctionsService;
import jakarta.annotation.Resource;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sakura
 */
@Service
public class AuctionsServiceImpl implements AuctionsService {

    @Resource
    private UsersMapper usersMapper;

    @Resource
    private WorksMapper worksMapper;

    @Resource
    private AuctionsMapper auctionsMapper;

    @Resource
    private Client client;

    @Value("${fisco.contract.address}")
    private String CONTRACT_ADDRESS;


    @Override
    @Transactional
    public void startAuction(String email, BigInteger workId, BigInteger startPrice, long duration, String privateKey) {
        // 验证用户是否存在
        QueryWrapper<UsersDO> userQuery = new QueryWrapper<>();
        userQuery.eq("email", email);
        UsersDO user = usersMapper.selectOne(userQuery);
        if (user == null || !"正常".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户不存在或状态异常，无法发起拍卖");
        }

        // 验证作品是否存在
        QueryWrapper<WorksDO> workQuery = new QueryWrapper<>();
        workQuery.eq("work_id", workId);
        WorksDO work = worksMapper.selectOne(workQuery);
        if (work == null) {
            throw new IllegalArgumentException("作品不存在");
        }

        // 验证作品是否已申请版权
        if (work.getDigitalCopyrightId() == null) {
            throw new IllegalArgumentException("该作品尚未申请版权");
        }

        // 验证作品是否已经在拍卖
        if (work.getIsOnAuction()) {
            throw new IllegalArgumentException("该作品已在拍卖中");
        }

        // 调用智能合约开始拍卖
        CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);

        // 调用智能合约方法开始拍卖
        TransactionReceipt receipt = digitalCopyright.startAuction(
                workId, startPrice, BigInteger.valueOf(duration*1000));

        // 检查链上交易是否成功
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + receipt.getStatus());
        }

        // 获取链上的拍卖ID
        BigInteger auctionCounter;
        try {
            auctionCounter = digitalCopyright.auctionCounter();
        } catch (ContractException e) {
            throw new RuntimeException("获取链上的拍卖ID失败: " + e.getMessage(), e);
        }

        // 更新作品的拍卖状态为拍卖中
        work.setIsOnAuction(true);
        worksMapper.updateById(work);

        // 获取当前时间戳（拍卖结束时间）
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(duration);

        // 获取链上交易哈希
        String transactionHash = receipt.getTransactionHash();

        // 打印auction对象，确认数据是否正确
        AuctionsDO auction = new AuctionsDO();
        auction.setWorkId(workId);
        auction.setSellerId(work.getUserId());  // 假设作品的用户ID就是拍卖者ID
        auction.setStartPrice(startPrice);
        auction.setCurrentPrice(startPrice);  // 初始价格即当前价格
        auction.setBuyerId(null);     // 尚未有出价者
        auction.setEndTime(endTime);          // 拍卖结束时间
        auction.setStatus(1);                 // 1 表示拍卖中
        auction.setTransactionHash(transactionHash);  // 链上交易哈希
        auction.setAuctionId(auctionCounter);    // 设置链上的拍卖ID

        // 确认auction对象的值
        System.out.println("拍卖对象: " + auction);

        // 插入拍卖记录到数据库
        try {
            int result = auctionsMapper.insert(auction);
            if (result == 0) {
                throw new RuntimeException("拍卖信息插入失败，返回值为0");
            }
            System.out.println("拍卖信息成功插入数据库！");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("插入拍卖信息到数据库失败: " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public void placeBid(String email, BigInteger auctionId, BigInteger bidAmount, String privateKey) {
        // 1. 获取用户信息
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null || !"正常".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        // 2. 获取拍卖信息（通过 work_id 查询）
        AuctionsDO auction = auctionsMapper.selectOne(new QueryWrapper<AuctionsDO>().eq("auction_id", auctionId));
        if (auction == null) {
            throw new IllegalArgumentException("拍卖记录不存在，auction_id=" + auctionId);
        }

        // 打印拍卖记录，检查拍卖状态
        if (auction.getStatus() == 0) {
            throw new IllegalArgumentException("拍卖已结束，无法继续竞拍");
        } else if (auction.getStatus() != 1) {
            throw new IllegalArgumentException("无效的拍卖状态");
        }
        // 3. 验证出价是否高于当前出价
        if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
            throw new IllegalArgumentException("出价必须高于当前最高出价");
        }

        // 4. 验证用户区块链地址
        String blockchainAddress = user.getBlockchainAddress();
        if (blockchainAddress == null || blockchainAddress.isEmpty()) {
            throw new IllegalArgumentException("用户的区块链地址为空");
        }

        // 5. 获取作品信息
        WorksDO work = worksMapper.selectOne(new QueryWrapper<WorksDO>().eq("work_id", auction.getWorkId()));
        if (work == null) {
            throw new IllegalArgumentException("作品不存在");
        }
        if (work.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("作品拥有者不能参与竞拍");
        }


        // 6. 调用智能合约进行出价
        CryptoKeyPair userKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, userKeyPair);
        TransactionReceipt receipt = digitalCopyright.placeBid(auctionId, bidAmount);

        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + receipt.getStatus());
        }

        // 7. 更新拍卖记录的当前出价和最高出价者
        auction.setCurrentPrice(bidAmount);
        auction.setBuyerId(user.getId());  // 更新最高出价者

        // 8. 使用 MyBatis-Plus 的 updateById 方法更新拍卖信息
        if (auctionsMapper.updateById(auction) == 0) {
            throw new RuntimeException("更新拍卖记录失败");
        }

        System.out.println("竞拍成功，当前出价: " + bidAmount);
    }

    @Override
    @Transactional
    public void endAuction(String email, BigInteger auctionId, String privateKey) {
        // 1. 获取用户信息
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null || !"正常".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        // 2. 获取拍卖信息（通过 auctionId 查询）
        AuctionsDO auction = auctionsMapper.selectOne(new QueryWrapper<AuctionsDO>().eq("auction_id", auctionId));
        if (auction == null) {
            throw new IllegalArgumentException("拍卖记录不存在，auction_id=" + auctionId);
        }

        // 3. 检查拍卖状态，确保拍卖进行中
        if (auction.getStatus() != 1) {
            throw new IllegalArgumentException("无效的拍卖状态，无法结束拍卖");
        }

        // 4. 确认当前用户是该拍卖的创建者
        if (!auction.getSellerId().equals(user.getId())) {
            throw new IllegalArgumentException("只有拍卖的发起者才能结束该拍卖");
        }

        // 5. 获取最高出价者信息
        UsersDO highestBidder = usersMapper.selectById(auction.getBuyerId());
        if (highestBidder == null) {
            throw new IllegalArgumentException("找不到最高出价者，拍卖无法结束");
        }

        // 6. 调用智能合约结束拍卖
        CryptoKeyPair userKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, userKeyPair);
        TransactionReceipt receipt = digitalCopyright.endAuction(auctionId);

        // 7. 检查链上交易状态，确保交易成功
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + decodeRevertOutput(receipt.getOutput()));
        }

        // 8. 更新作品的拥有者（将作品的 userId 改为最高出价者的 ID）
        WorksDO work = worksMapper.selectOne(new QueryWrapper<WorksDO>().eq("work_id", auction.getWorkId()));
        if (work == null) {
            throw new IllegalArgumentException("作品不存在，无法更改拥有者");
        }

        // 更新作品拥有者为最高出价者
        work.setUserId(highestBidder.getId());
        if (worksMapper.updateById(work) == 0) {
            throw new RuntimeException("更新作品拥有者失败");
        }

        // 9. 更新拍卖记录状态为已结束
        auction.setStatus(0);  // 0表示拍卖已结束
        if (auctionsMapper.updateById(auction) == 0) {
            throw new RuntimeException("更新拍卖记录失败，无法结束拍卖");
        }

        System.out.println("拍卖结束成功，拍卖ID: " + auctionId + "，作品已转交给最高出价者: " + highestBidder.getEmail());
    }


    // 解码 output（ABI 编码的错误消息）
    private String decodeRevertOutput(String output) {
        // 输出的格式通常是：'0x08c379a0...'，8个字节是错误标识符，后面是错误消息
        if (output == null || output.length() <= 10) {
            return null;
        }

        String data = output.substring(10); // 去掉前缀 '0x08c379a0'

        // 解码：将 hex 转换为 UTF-8 字符串
        byte[] bytes = Numeric.hexStringToByteArray(data);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getAllAuctions() {
        // 查询所有已申请拍卖的作品数据 (is_on_auction = 1)
        QueryWrapper<WorksDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_on_auction", 1); // 只查询已申请拍卖的作品

        List<WorksDO> worksList = worksMapper.selectList(queryWrapper);

        // 创建一个List来存放最终的返回结果
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 为每个作品记录填充对应的作品标题、描述以及区块哈希信息
        for (WorksDO work : worksList) {
            Map<String, Object> auctionMap = new HashMap<>();

            // 填充作品的标题、描述和区块哈希到返回结果Map中
            auctionMap.put("title", work.getTitle());  // 作品标题
            auctionMap.put("description", work.getDescription());  // 作品描述
            auctionMap.put("blockHash", work.getBlockchainHash());  // 区块哈希
            auctionMap.put("imagePath", work.getImgUrl());  // 区块哈希

            // 将当前作品信息Map添加到返回结果列表中
            resultList.add(auctionMap);
        }

        // 返回封装好的结果
        return resultList;
    }


}
