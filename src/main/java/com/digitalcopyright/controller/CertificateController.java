package com.digitalcopyright.controller;

import com.digitalcopyright.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;


/**
 * 证书 Controller
 * @author Sakura
 */
@RestController
@RequestMapping("/certificate")

@Slf4j
public class CertificateController {

    private final CertificateService certificateService; // 注入证书服务，用于处理证书生成与下载逻辑

    /**
     * 构造函数
     * @param certificateService 注入的证书服务实例
     */
    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * 下载证书
     * 根据作品 ID (workId) 生成并返回对应的版权证书 PDF 文件。
     * @param workId 作品 ID，用于标识需要下载证书的作品
     * @return 包含 PDF 文件字节流的 ResponseEntity 响应
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadCertificate(@RequestParam BigInteger workId) {
        try {
            // 调用服务层方法，根据作品 ID 获取证书的字节流
            byte[] pdfBytes = certificateService.downloadCertificate(workId);

            // 设置 HTTP 响应头，指定内容类型为 PDF，附件下载的文件名为 "copyright_certificate.pdf"
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF); // 设置响应内容类型为 PDF
            headers.setContentDispositionFormData("attachment", "copyright_certificate.pdf"); // 设置文件下载的文件名

            // 返回包含 PDF 数据的响应
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            // 捕获异常，打印堆栈信息，并返回 HTTP 500 错误响应
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
