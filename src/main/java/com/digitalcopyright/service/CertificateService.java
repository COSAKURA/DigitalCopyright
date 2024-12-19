package com.digitalcopyright.service;

import com.digitalcopyright.utils.R;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author Sakura
 */
@Service
public interface CertificateService {

    byte[] downloadCertificate(BigInteger workId);


    byte[] downloadCertificate2(BigInteger workId);
}
