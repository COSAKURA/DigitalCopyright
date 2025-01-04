package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.common.AuctionWebSocketHandler;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.AuctionsMapper;
import com.digitalcopyright.mapper.PriceMapper;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.mapper.WorksMapper;
import com.digitalcopyright.model.DO.AuctionsDO;
import com.digitalcopyright.model.DO.PriceDO;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.service.AuctionsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // 用于操作用户表（`users`）的 MyBatis Mapper，提供用户信息的增删改查功能。
    @Resource
    private UsersMapper usersMapper;

    // 用于操作作品表（`works`）的 MyBatis Mapper，负责管理作品相关的数据库记录。
    @Resource
    private WorksMapper worksMapper;


    @Resource
    private AuctionsMapper auctionsMapper;

    // 用于操作竞价记录表（`price`）的 MyBatis Mapper，负责管理竞价数据
    @Resource
    private PriceMapper priceMapper;

    // 区块链客户端，用于与区块链网络交互
    @Resource
    private Client client;

    // 从配置文件中读取区块链智能合约地址，用于标识需要调用的目标智能合约。
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
            // 如果用户不存在或用户状态异常，抛出异常
            throw new IllegalArgumentException("用户不存在或状态异常，无法发起拍卖");
        }

        // 验证作品是否存在
        QueryWrapper<WorksDO> workQuery = new QueryWrapper<>();
        workQuery.eq("work_id", workId);
        WorksDO work = worksMapper.selectOne(workQuery);
        if (work == null) {
            // 如果作品不存在，抛出异常
            throw new IllegalArgumentException("作品不存在");
        }

        // 验证作品是否已申请版权
        if (work.getDigitalCopyrightId() == null) {
            // 如果作品未申请版权，抛出异常
            throw new IllegalArgumentException("该作品尚未申请版权");
        }

        // 验证作品是否已经在拍卖中
        if (work.getIsOnAuction()) {
            // 如果作品已在拍卖中，抛出异常
            throw new IllegalArgumentException("该作品已在拍卖中");
        }

        // 调用智能合约开始拍卖
        // 创建区块链私钥对（使用传入的用户私钥）
        CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        // 加载智能合约实例
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);

        // 调用智能合约方法开始拍卖
        TransactionReceipt receipt = digitalCopyright.startAuction(
                workId, startPrice, BigInteger.valueOf(duration * 1000));

        // 检查链上交易是否成功（交易状态为 0x0 表示成功）
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + receipt.getStatus());
        }

        // 获取链上的拍卖ID
        BigInteger auctionCounter;
        try {
            auctionCounter = digitalCopyright.auctionCounter();
        } catch (ContractException e) {
            // 如果获取链上的拍卖ID失败，抛出异常
            throw new RuntimeException("获取链上的拍卖ID失败: " + e.getMessage(), e);
        }

        // 更新作品的拍卖状态为拍卖中
        work.setIsOnAuction(true);
        worksMapper.updateById(work);

        // 计算拍卖结束时间（当前时间 + 持续时间）
        LocalDateTime endTime = LocalDateTime.now().plusSeconds(duration);

        // 获取链上交易的哈希值
        String transactionHash = receipt.getTransactionHash();

        // 创建拍卖记录对象
        AuctionsDO auction = new AuctionsDO();
        auction.setWorkId(workId); // 设置作品ID
        auction.setSellerId(work.getUserId()); // 设置卖家ID（假设作品拥有者即为卖家）
        auction.setStartPrice(startPrice); // 设置起拍价
        auction.setCurrentPrice(startPrice); // 当前价格初始为起拍价
        auction.setBuyerId(null); // 尚未有出价者
        auction.setEndTime(endTime); // 设置拍卖结束时间
        auction.setStatus(1); // 设置拍卖状态为进行中（1表示进行中）
        auction.setTransactionHash(transactionHash); // 设置链上交易哈希
        auction.setAuctionId(auctionCounter); // 设置链上的拍卖ID

        // 打印拍卖对象，供调试使用
        System.out.println("拍卖对象: " + auction);

        // 插入拍卖记录到数据库
        try {
            int result = auctionsMapper.insert(auction);
            if (result == 0) {
                // 如果插入失败（返回值为0），抛出异常
                throw new RuntimeException("拍卖信息插入失败，返回值为0");
            }
            System.out.println("拍卖信息成功插入数据库！");
        } catch (Exception e) {
            // 捕获并打印插入数据库时的异常
            e.printStackTrace();
            throw new RuntimeException("插入拍卖信息到数据库失败: " + e.getMessage());
        }
    }



    @Override
    @Transactional
    public void placeBid(String email, BigInteger auctionId, BigInteger bidAmount, String privateKey) throws JsonProcessingException {
        // 1. 获取用户信息
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null || !"正常".equals(user.getStatus())) {
            // 如果用户不存在或用户状态异常，抛出异常
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        // 2. 获取拍卖信息（通过 auction_id 查询）
        AuctionsDO auction = auctionsMapper.selectOne(new QueryWrapper<AuctionsDO>().eq("auction_id", auctionId));
        if (auction == null) {
            // 如果拍卖记录不存在，抛出异常
            throw new IllegalArgumentException("拍卖记录不存在，auction_id=" + auctionId);
        }

        // 检查拍卖状态是否有效
        if (auction.getStatus() == 0) {
            // 如果拍卖已结束，抛出异常
            throw new IllegalArgumentException("拍卖已结束，无法继续竞拍");
        } else if (auction.getStatus() != 1) {
            // 如果拍卖状态无效，抛出异常
            throw new IllegalArgumentException("无效的拍卖状态");
        }

        // 3. 验证出价是否高于当前最高出价
        if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
            // 如果出价低于或等于当前最高出价，抛出异常
            throw new IllegalArgumentException("出价必须高于当前最高出价");
        }

        // 4. 验证用户区块链地址
        String blockchainAddress = user.getBlockchainAddress();
        if (blockchainAddress == null || blockchainAddress.isEmpty()) {
            // 如果用户区块链地址为空，抛出异常
            throw new IllegalArgumentException("用户的区块链地址为空");
        }

        // 5. 获取作品信息
        WorksDO work = worksMapper.selectOne(new QueryWrapper<WorksDO>().eq("work_id", auction.getWorkId()));
        if (work == null) {
            // 如果作品不存在，抛出异常
            throw new IllegalArgumentException("作品不存在");
        }
        if (work.getUserId().equals(user.getId())) {
            // 如果出价者是作品的拥有者，抛出异常
            throw new IllegalArgumentException("作品拥有者不能参与竞拍");
        }

        // 6. 调用智能合约进行出价
        // 创建用户私钥对，用于签名交易
        CryptoKeyPair userKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        // 加载智能合约实例
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, userKeyPair);
        // 调用智能合约的出价方法
        TransactionReceipt receipt = digitalCopyright.placeBid(auctionId, bidAmount);

        // 检查链上交易状态
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + receipt.getStatus());
        }

        // 获取交易哈希
        String transactionHash = receipt.getTransactionHash();

        // 7. 更新拍卖记录的当前出价和最高出价者
        auction.setCurrentPrice(bidAmount); // 更新当前最高出价
        auction.setBuyerId(user.getId());   // 更新最高出价者 ID

        // 更新拍卖记录到数据库
        if (auctionsMapper.updateById(auction) == 0) {
            throw new RuntimeException("更新拍卖记录失败");
        }

        // 8. 插入竞价记录到 price 表
        PriceDO price = new PriceDO();
        price.setAuctionId(auctionId);         // 设置拍卖 ID
        price.setUserId(user.getId());         // 设置出价者 ID
        price.setPrice(bidAmount);             // 设置出价金额
        price.setWorkId(auction.getWorkId());  // 设置作品 ID
        price.setTransactionHash(transactionHash); // 设置链上交易哈希
        price.setCreatedAt(LocalDateTime.now());  // 设置出价时间

        // 插入竞价记录到数据库
        if (priceMapper.insert(price) == 0) {
            throw new RuntimeException("插入竞价记录失败");
        }

        // 出价成功后，推送更新给所有客户端
        Map<String, Object> auctionUpdate = new HashMap<>();
        auctionUpdate.put("auctionId", auctionId);       // 拍卖 ID
        auctionUpdate.put("currentPrice", bidAmount);    // 当前最高出价
        auctionUpdate.put("buyerId", user.getId());      // 最高出价者 ID

        // 将更新数据转换为 JSON 格式
        String message = new ObjectMapper().writeValueAsString(auctionUpdate);

        // 推送消息到 WebSocket，通知所有客户端
        AuctionWebSocketHandler.broadcast(message);
    }



    @Override
    @Transactional
    public void endAuction(String email, BigInteger auctionId, String privateKey) {
        // 1. 获取用户信息
        // 根据邮箱查询用户信息，确保用户存在并状态正常
        UsersDO user = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("email", email));
        if (user == null || !"正常".equals(user.getStatus())) {
            // 如果用户不存在或状态异常，抛出异常
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        // 2. 获取拍卖信息
        // 根据拍卖 ID 查询拍卖记录
        AuctionsDO auction = auctionsMapper.selectOne(new QueryWrapper<AuctionsDO>().eq("auction_id", auctionId));
        if (auction == null) {
            // 如果拍卖记录不存在，抛出异常
            throw new IllegalArgumentException("拍卖记录不存在，auction_id=" + auctionId);
        }

        // 3. 检查拍卖状态
        // 验证拍卖是否正在进行中（状态为 1 表示进行中）
        if (auction.getStatus() != 1) {
            throw new IllegalArgumentException("拍卖已结束");
        }

        // 4. 验证用户是否为拍卖的创建者
        // 确保当前用户是拍卖的卖家
        if (!auction.getSellerId().equals(user.getId())) {
            throw new IllegalArgumentException("当前用户不是该拍卖的创建者");
        }

        // 5. 获取最高出价者信息
        // 根据最高出价者的 ID 查询用户信息
        UsersDO highestBidder = usersMapper.selectById(auction.getBuyerId());
        if (highestBidder == null) {
            // 如果找不到最高出价者，抛出异常
            throw new IllegalArgumentException("找不到最高出价者，拍卖结束");
        }

        // 6. 调用智能合约结束拍卖
        // 使用用户的私钥创建区块链密钥对
        CryptoKeyPair userKeyPair = client.getCryptoSuite().createKeyPair(privateKey);
        // 加载智能合约实例
        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, userKeyPair);
        // 调用合约的 `endAuction` 方法结束拍卖
        TransactionReceipt receipt = digitalCopyright.endAuction(auctionId);

        // 7. 检查链上交易状态
        // 验证链上交易是否成功（状态为 0x0 表示成功）
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上交易失败，交易状态: " + decodeRevertOutput(receipt.getOutput()));
        }

        // 8. 更新作品拥有者信息
        // 获取拍卖对应的作品信息
        WorksDO work = worksMapper.selectOne(new QueryWrapper<WorksDO>().eq("work_id", auction.getWorkId()));
        if (work == null) {
            throw new IllegalArgumentException("作品不存在，无法更改拥有者");
        }

        // 将作品的拥有者更改为最高出价者，同时设置作品不再处于拍卖状态
        work.setUserId(highestBidder.getId());
        work.setIsOnAuction(false);
        if (worksMapper.updateById(work) == 0) {
            throw new RuntimeException("更新作品信息失败，无法更改拥有者或结束拍卖状态");
        }

        // 9. 更新拍卖记录状态为已结束
        // 将拍卖状态设置为 0（已结束）
        auction.setStatus(0);
        if (auctionsMapper.updateById(auction) == 0) {
            throw new RuntimeException("更新拍卖记录失败，无法结束拍卖");
        }

        // 打印结束拍卖成功信息
        System.out.println("拍卖结束成功，拍卖ID: " + auctionId + "，作品已转交给最高出价者: " + highestBidder.getEmail());
    }



    // 解码 output（ABI 编码的错误消息）
    private String decodeRevertOutput(String output) {
        // 输出的格式通常是：'0x08c379a0...'，8个字节是错误标识符，后面是错误消息
        if (output == null || output.length() <= 10) {
            return null;
        }

        // 去掉前缀 '0x08c379a0'
        String data = output.substring(10);

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
        // 查询拍卖表中所有进行中的拍卖 (status = 1)
        QueryWrapper<AuctionsDO> auctionWrapper = new QueryWrapper<>();
        // 条件：只查询状态为 "进行中" 的拍卖
        auctionWrapper.eq("status", 1);

        // 获取进行中的拍卖列表
        List<AuctionsDO> auctionList = auctionsMapper.selectList(auctionWrapper);

        // 创建一个 List 来存放最终的返回结果
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 遍历拍卖列表，为每个拍卖填充详细信息
        for (AuctionsDO auction : auctionList) {
            Map<String, Object> auctionMap = new HashMap<>();

            // 查询作品信息：根据 workId 获取作品详情
            WorksDO work = worksMapper.selectOne(new QueryWrapper<WorksDO>().eq("work_id", auction.getWorkId()));

            // 查询用户信息：根据卖家ID (sellerId) 获取用户详情
            UsersDO seller = usersMapper.selectOne(new QueryWrapper<UsersDO>().eq("id", auction.getSellerId()));

            // 填充拍卖和作品的详细信息
            auctionMap.put("auctionId", auction.getAuctionId()); // 拍卖ID
            auctionMap.put("workId", auction.getWorkId());       // 作品ID
            auctionMap.put("startingPrice", auction.getStartPrice()); // 起始价格
            auctionMap.put("currentPrice", auction.getCurrentPrice()); // 当前价格
            auctionMap.put("endTime", auction.getEndTime());     // 拍卖结束时间

            // 填充作品信息
            if (work != null) {
                auctionMap.put("title", work.getTitle());               // 作品标题
                auctionMap.put("blockHash", work.getBlockchainHash());  // 区块链哈希
                auctionMap.put("imagePath", work.getImgUrl());          // 作品图片路径
                auctionMap.put("category", work.getCategory());         // 作品类别
            } else {
                auctionMap.put("title", "未知作品");                    // 如果作品信息缺失
                auctionMap.put("blockHash", "未知");
                auctionMap.put("imagePath", "");
                auctionMap.put("category", "未知类别");
            }

            // 填充用户信息
            auctionMap.put("username", (seller != null) ? seller.getUsername() : "未知用户"); // 卖家用户名

            // 将当前拍卖信息 Map 添加到结果列表中
            resultList.add(auctionMap);
        }

        // 返回封装好的结果列表
        return resultList;
    }




    @Override
    public Map<String, Object> getAuctionById(Integer workId, String currentUserEmail) {
        // 初始化结果 Map，用于存放拍卖、作品、用户及出价记录的详细信息
        Map<String, Object> result = new HashMap<>();

        // 查询拍卖数据（只查询状态为 1 的拍卖，表示拍卖正在进行中）
        QueryWrapper<AuctionsDO> auctionWrapper = new QueryWrapper<>();
        auctionWrapper.eq("work_id", workId);
        auctionWrapper.eq("status", 1); // 拍卖状态为进行中
        AuctionsDO auction = auctionsMapper.selectOne(auctionWrapper);

        if (auction == null) {
            // 如果没有找到对应的拍卖信息，抛出异常
            throw new RuntimeException("未找到对应的正在进行中的拍卖信息");
        }

        // 查询作品数据（根据 workId）
        QueryWrapper<WorksDO> workWrapper = new QueryWrapper<>();
        workWrapper.eq("work_id", workId);
        WorksDO work = worksMapper.selectOne(workWrapper);

        if (work == null) {
            // 如果没有找到对应的作品信息，抛出异常
            throw new RuntimeException("未找到对应的作品信息");
        }

        // 查询用户数据（根据作品的 user_id 字段获取用户信息）
        QueryWrapper<UsersDO> userWrapper = new QueryWrapper<>();
        userWrapper.eq("id", work.getUserId());
        UsersDO user = usersMapper.selectOne(userWrapper);

        if (user == null) {
            // 如果没有找到对应的用户信息，抛出异常
            throw new RuntimeException("未找到对应的用户信息");
        }

        // 判断当前用户是否是拍卖的发起人（通过比较用户邮箱）
        boolean isOwner = currentUserEmail.equals(user.getEmail());

        // 查询出价记录（根据拍卖 ID 查询 price 表）
        QueryWrapper<PriceDO> priceWrapper = new QueryWrapper<>();
        priceWrapper.eq("auction_id", auction.getAuctionId()); // 根据拍卖 ID 查询
        priceWrapper.orderByDesc("created_at"); // 按创建时间倒序排序
        List<PriceDO> priceList = priceMapper.selectList(priceWrapper);

        // 构造出价记录列表
        List<Map<String, Object>> bids = new ArrayList<>();
        for (PriceDO price : priceList) {
            Map<String, Object> bid = new HashMap<>();

            // 查询出价用户信息
            UsersDO bidder = usersMapper.selectById(price.getUserId());
            if (bidder != null) {
                bid.put("username", bidder.getUsername()); // 出价者用户名
            }
            bid.put("price", price.getPrice());               // 出价金额
            bid.put("transactionHash", price.getTransactionHash()); // 链上交易哈希
            bid.put("createdAt", price.getCreatedAt());       // 出价时间

            bids.add(bid);
        }

        // 封装拍卖数据、作品数据和用户数据到结果 Map 中
        result.put("auctionId", auction.getAuctionId());          // 拍卖 ID
        result.put("title", work.getTitle());                     // 作品标题
        result.put("description", work.getDescription());         // 作品描述
        result.put("imgUrl", work.getImgUrl());                   // 作品图片 URL
        result.put("digitalCopyrightId", work.getDigitalCopyrightId()); // 作品版权 ID
        result.put("startPrice", auction.getStartPrice());        // 起拍价
        result.put("currentPrice", auction.getCurrentPrice());    // 当前最高出价
        result.put("endTime", auction.getEndTime());              // 拍卖结束时间
        result.put("username", user.getUsername());               // 卖家用户名
        result.put("isOwner", isOwner);                           // 是否当前用户为发起人
        result.put("bids", bids);                                 // 出价记录列表

        // 返回封装好的结果
        return result;
    }

}
