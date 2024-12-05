package com.digitalcopyright.service;

import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * @author Sakura
 */
@Service
public interface CertificateService {

    byte[] downloadCertificate(BigInteger workId);
}
