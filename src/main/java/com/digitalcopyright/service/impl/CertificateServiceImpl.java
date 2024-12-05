package com.digitalcopyright.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.digitalcopyright.fisco.DigitalCopyright;
import com.digitalcopyright.mapper.UsersMapper;
import com.digitalcopyright.model.DO.UsersDO;
import com.digitalcopyright.service.CertificateService;
import com.digitalcopyright.utils.PdfUtil;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple10;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class CertificateServiceImpl implements CertificateService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Client client;

    @Value("${fisco.contract.address}")
    private String CONTRACT_ADDRESS;

    @Value("${fisco.contract.adminPrivateKey}")
    private String adminPrivate;

    @Override
    public byte[] downloadCertificate(BigInteger workId) {
        try {
            // 加载合约实例
            CryptoKeyPair adminKeyPair = client.getCryptoSuite().createKeyPair(adminPrivate);
            DigitalCopyright contractInstance = DigitalCopyright.load(CONTRACT_ADDRESS, client, adminKeyPair);

            // 验证作品 ID
            BigInteger workCount = contractInstance.workCounter();
            if (workId.compareTo(BigInteger.ZERO) <= 0 || workId.compareTo(workCount) > 0) {
                throw new IllegalArgumentException("无效的作品 ID: " + workId);
            }

            // 获取链上作品详情
            Tuple10<BigInteger, String, String, String, String, String, BigInteger, String, BigInteger, Boolean> details =
                    contractInstance.getWorkDetails(workId);

            // 验证用户信息
            String userAddress = details.getValue5();
            QueryWrapper<UsersDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("blockchain_address", userAddress);
            UsersDO user = usersMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new IllegalArgumentException("作品所属用户不存在，地址: " + userAddress);
            }

            // 将合约返回的详情直接写入 PDF
            return PdfUtil.generatePdf(
                    user.getUsername(), // 用户名
                    details.getValue1().toString(), // 创建时间
                    details.getValue2(), // 标题
                    details.getValue3(), // 描述
                    details.getValue5(), // 拥有者
                    details.getValue4(), // 作品哈希
                    details.getValue4(), // 版权编号
                    details.getValue6(), // 审核地址
                    details.getValue9().toString(), // 作品 ID
                    details.getValue4() // 二维码内容（可以替换为作品链接）
            );

        } catch (Exception e) {
            throw new RuntimeException("获取作品信息或生成 PDF 失败: " + e.getMessage(), e);
        }
    }
}
