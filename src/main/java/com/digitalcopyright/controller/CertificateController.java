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

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    /**
     * 下载证书
     * @param workId 作品id
     * @return ResponseEntity<byte[]>
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadCertificate(@RequestParam BigInteger workId) {
        try {
            byte[] pdfBytes = certificateService.downloadCertificate(workId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "copyright_certificate.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
