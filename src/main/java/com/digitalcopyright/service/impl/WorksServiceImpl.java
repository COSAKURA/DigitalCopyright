package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.mapper.WorksMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.model.DTO.RegisterWorkDTO;
import com.digitalcopyright.model.VO.WorkDetailsVO;
import com.digitalcopyright.service.WorksService;
import com.digitalcopyright.utils.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple10;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Random;

import static org.fisco.bcos.sdk.utils.AddressUtils.isValidAddress;

@Service
@Slf4j
public class WorksServiceImpl implements WorksService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private WorksMapper worksMapper;

    @Autowired
    private Client client;

    @Value("${fisco.contract.address}")
    private  String CONTRACT_ADDRESS;

    @Value("${fisco.contract.adminPrivateKey}")
    private  String adminPrivate;

    @Override
    public void registerWork(RegisterWorkDTO registerWorkDTO) {
        // 根据邮箱查找用户
        UsersDO user = fetchUserByEmail(registerWorkDTO.getEmail());

        try {
            // 保存图片并获取文件路径
            Path filePath = saveImageToLocal(registerWorkDTO.getImg());

            // 计算图片哈希值
            String hash = calculateImageHash(filePath);

            // 检查哈希值是否已存在
            validateImageHash(hash);

            // 从 DTO 获取用户私钥并生成 KeyPair
            CryptoKeyPair userKeyPair = createKeyPair(registerWorkDTO.getPrivateKey());

            // 加载智能合约
            DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, userKeyPair);

            // 调用智能合约上传作品
            TransactionReceipt receipt = digitalCopyright.registerWork(
                    registerWorkDTO.getTitle(),
                    registerWorkDTO.getDescription(),
                    hash
            );

            // 获取链上返回的作品 ID 和交易哈希
            BigInteger workIdOnChain = getWorkIdFromReceipt(digitalCopyright);

            // 保存作品信息到数据库
            saveWorkToDatabase(registerWorkDTO, user, filePath, hash, workIdOnChain, receipt.getTransactionHash());
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出业务异常
        } catch (Exception e) {
            throw new RuntimeException("作品注册失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据邮箱获取用户信息
     */
    private UsersDO fetchUserByEmail(String email) {
        QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        UsersDO user = usersMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    /**
     * 保存图片到本地并返回文件路径
     */
    private Path saveImageToLocal(MultipartFile img) throws Exception {
        // 使用项目根目录动态指定上传路径
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";

        // 创建文件夹（如果不存在）
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new RuntimeException("创建目录失败");
            }
        }

        String filename = System.currentTimeMillis() + "_" + img.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, filename);

        // 验证文件类型
        String contentType = img.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("文件类型不支持，仅支持 jpg, png, gif, bmp, webp 格式的图片");
        }

        Files.write(filePath, img.getBytes()); // 保存文件到指定路径
        return filePath;
    }

    /**
     * 验证图片类型
     */
    private boolean isValidImageType(String contentType) {
        return "image/jpeg".equals(contentType) ||
                "image/png".equals(contentType) ||
                "image/gif".equals(contentType) ||
                "image/bmp".equals(contentType) ||
                "image/webp".equals(contentType);
    }

    /**
     * 计算图片哈希值
     */
    private String calculateImageHash(Path filePath) throws Exception {
        String base64Encoded = EncryptionUtil.encodeBase64(filePath); // Base64 编码
        return EncryptionUtil.calculateSHA256(base64Encoded); // 计算哈希值
    }

    /**
     * 检查哈希值是否已存在
     */
    private void validateImageHash(String hash) {
        QueryWrapper<WorksDO> hashQuery = new QueryWrapper<>();
        hashQuery.eq("hash", hash);
        WorksDO existingWork = worksMapper.selectOne(hashQuery);
        if (existingWork != null) {
            throw new IllegalArgumentException("该图片已注册，无法重复注册");
        }
    }

    /**
     * 创建 CryptoKeyPair
     */
    private CryptoKeyPair createKeyPair(String privateKey) {
        if (privateKey == null || privateKey.isEmpty()) {
            throw new IllegalArgumentException("用户私钥不能为空");
        }
        return client.getCryptoSuite().createKeyPair(privateKey);
    }

    /**
     * 保存作品信息到数据库
     */
    private void saveWorkToDatabase(RegisterWorkDTO dto, UsersDO user, Path filePath, String hash, BigInteger workIdOnChain, String transactionHash) {
        WorksDO work = new WorksDO();
        work.setUserId(user.getId());
        work.setTitle(dto.getTitle());
        work.setDescription(dto.getDescription());
        work.setImgUrl(filePath.toString());
        work.setHash(hash);
        work.setWorkId(workIdOnChain.intValue());
        work.setBlockchainHash(hash);
        work.setTransactionHash(transactionHash);
        work.setCreatedAt(LocalDateTime.now());
        worksMapper.insert(work);
    }

    /**
     * 从 TransactionReceipt 获取链上作品 ID
     */
    private BigInteger getWorkIdFromReceipt(DigitalCopyright digitalCopyright) {
        try {
            BigInteger workId = digitalCopyright.workCounter();
            if (workId == null) {
                throw new RuntimeException("获取最新作品 ID 失败，workCounter 返回为空");
            }
            return workId;
        } catch (Exception e) {
            throw new RuntimeException("获取链上作品 ID 失败: " + e.getMessage(), e);
        }
    }


    @Override
    public WorkDetailsVO getWorkDetails(BigInteger workId) {
        try {
            // 加载合约实例
            CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(adminPrivate);
            DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);
            log.info("加载合约成功，地址: {}", CONTRACT_ADDRESS);
            // 验证作品 ID 是否有效
            BigInteger workCount = digitalCopyright.workCounter();
            if (workId.compareTo(BigInteger.ZERO) <= 0 || workId.compareTo(workCount) > 0) {
                throw new IllegalArgumentException("无效的作品 ID: " + workId);
            }

            // 获取作品详情
            var details = digitalCopyright.getWorkDetails(workId);

            // 验证区块链地址
            String userAddress = details.getValue5();
            if (!isValidAddress(userAddress)) {
                throw new IllegalArgumentException("无效的区块链地址: " + userAddress);
            }

            // 查询用户信息
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("blockchain_address", userAddress);
            UsersDO user = usersMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new IllegalArgumentException("作品所属用户不存在，地址: " + userAddress);
            }

            // 封装作品详情
            return buildWorkDetailsVO(details);

        } catch (IllegalArgumentException e) {
            log.error("查询作品详情参数异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("查询作品详情失败，作品 ID: {}, 错误: {}", workId, e.getMessage(), e);
            throw new RuntimeException("查询作品详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建 WorkDetailsVO 对象
     */
    private WorkDetailsVO buildWorkDetailsVO(
            Tuple10<BigInteger, String, String, String, String, String, BigInteger, String, BigInteger, Boolean> details) {

        WorkDetailsVO workDetailsVO = new WorkDetailsVO();
        workDetailsVO.setWorkId(details.getValue1().intValue());
        workDetailsVO.setTitle(details.getValue2());
        workDetailsVO.setDescription(details.getValue3());
        workDetailsVO.setHash(details.getValue4());
        workDetailsVO.setUserAddress(details.getValue5());
        workDetailsVO.setReviewer(details.getValue6());
        workDetailsVO.setStatus(details.getValue7().intValue());
        workDetailsVO.setBlockchainHash(details.getValue8());
        workDetailsVO.setCreatedAt(LocalDateTime.ofEpochSecond(details.getValue9().longValue(), 0, ZoneOffset.UTC));
        workDetailsVO.setIsOnAuction(details.getValue10());

        log.info("封装作品详情完成: {}", workDetailsVO);
        return workDetailsVO;
    }


    @Override
    public void applyCopyright(String email, BigInteger workId) {
        // 验证用户是否存在
        QueryWrapper<UsersDO> userQuery = new QueryWrapper<>();
        userQuery.eq("email", email);
        UsersDO user = usersMapper.selectOne(userQuery);
        if (user == null || !"正常".equals(user.getStatus())) {
            throw new IllegalArgumentException("用户不存在或状态异常，无法申请版权");
        }

        // 验证作品是否存在 (根据 work_id 字段)
        QueryWrapper<WorksDO> workQuery = new QueryWrapper<>();
        workQuery.eq("work_id", workId);
        WorksDO work = worksMapper.selectOne(workQuery);
        if (work == null) {
            throw new IllegalArgumentException("作品不存在");
        }

        if (work.getDigitalCopyrightId() != null) {
            throw new IllegalArgumentException("该作品已申请过版权");
        }

        // 验证作品是否属于用户
        if (!work.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("作品不属于该用户，无法申请版权");
        }

        // 生成版权编号
        String copyrightId = generateCopyrightId(work.getCreatedAt(), LocalDateTime.now());

        // 调用智能合约进行版权上链
        CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(adminPrivate);

        DigitalCopyright digitalCopyright = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);


        TransactionReceipt receipt = digitalCopyright.reviewCopyright(
                BigInteger.valueOf(work.getWorkId()),
                copyrightId
        );

        // 检查链上交易是否成功
        if (!"0x0".equals(receipt.getStatus())) {
            throw new RuntimeException("链上更新失败，交易状态: " + receipt.getStatus());
        }

        // 如果链上操作成功，更新数据库
        work.setDigitalCopyrightId(copyrightId);
        work.setStatus(1); // 假设 1 表示 "已授权"
        worksMapper.updateById(work);
    }


    /**
     * 生成版权编号
     * 格式: Copr + 开始年份 + 最新作品年份 + 7位流水号
     */
    public String generateCopyrightId(LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt == null || now == null) {
            throw new IllegalArgumentException("生成版权编号的时间参数不能为空");
        }

        String startYear = String.valueOf(createdAt.getYear());
        String currentYear = String.valueOf(now.getYear());

        // 使用当前时间戳的秒数和随机数生成唯一编号
        long timestamp = System.currentTimeMillis() / 1000;
        int randomPart = new Random().nextInt(100000); // 5 位随机数

        return "Copr" + startYear + currentYear + String.format("%07d", timestamp % 10000000 + randomPart);
    }


}

