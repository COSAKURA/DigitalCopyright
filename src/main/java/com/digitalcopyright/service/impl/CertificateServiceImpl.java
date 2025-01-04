package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.mapper.WorksMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.model.DO.WorksDO;
import com.digitalcopyright.service.CertificateService;
import com.digitalcopyright.utils.PdfUtil;
import jakarta.annotation.Resource;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple10;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * 实现作品证书下载服务
 * 根据作品 ID 从区块链上获取作品详情，并生成 PDF 证书文件。
 * 作者: Sakura
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    @Resource
    private UsersMapper usersMapper; // 用户表操作类

    @Resource
    private WorksMapper worksMapper; // 作品表操作类

    @Resource
    private Client client; // 区块链客户端

    @Value("${fisco.contract.address}")
    private String CONTRACT_ADDRESS; // 区块链合约地址

    @Value("${fisco.contract.adminPrivateKey}")
    private String adminPrivate; // 区块链管理员私钥

    /**
     * 根据作品 ID 下载作品证书
     * @param workId 作品 ID
     * @return 生成的 PDF 文件字节数组
     */
    @Override
    public byte[] downloadCertificate(BigInteger workId) {
        try {
            // 1. 加载区块链智能合约实例
            CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(adminPrivate);
            DigitalCopyright contractInstance = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);

            // 2. 验证作品 ID 的合法性
            BigInteger workCount = contractInstance.workCounter();
            if (workId.compareTo(BigInteger.ZERO) <= 0 || workId.compareTo(workCount) > 0) {
                throw new IllegalArgumentException("无效的作品 ID: " + workId);
            }

            // 3. 从区块链获取作品详情
            Tuple10<BigInteger, String, String, String, String, String, BigInteger, String, BigInteger, Boolean> details =
                    contractInstance.getWorkDetails(workId);

            // 4. 验证用户信息
            String userAddress = details.getValue5(); // 从合约中获取作品所属用户的区块链地址
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("blockchain_address", userAddress); // 根据区块链地址查询用户表
            UsersDO user = usersMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new IllegalArgumentException("作品所属用户不存在，地址: " + userAddress);
            }

            // 5. 从数据库查询作品的类别信息
            QueryWrapper<WorksDO> workQuery = new QueryWrapper<>();
            workQuery.eq("work_id", workId);
            WorksDO work = worksMapper.selectOne(workQuery);

            if (work == null) {
                throw new IllegalArgumentException("作品信息不存在，ID: " + workId);
            }

            // 6. 调用 PDF 工具生成证书文件
            return PdfUtil.generatePdfWithTemplate(
                    user.getUsername(),               // 用户名
                    details.getValue1().toString(),   // 创建时间
                    details.getValue2(),              // 作品标题
                    String.valueOf(details.getValue8()), // 版权编号
                    details.getValue6(),              // 审核地址
                    details.getValue9().toString(),   // 作品 ID
                    work.getCategory(),               // 作品类别
                    details.getValue4()               // 二维码内容（通常为作品链接或版权信息）
            );
        } catch (Exception e) {
            // 捕获异常并抛出运行时异常，带有详细错误信息
            throw new RuntimeException("获取作品信息或生成 PDF 失败: " + e.getMessage(), e);
        }
    }
}

