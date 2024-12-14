package com.digitalcopyright.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


/**
 * 服务类：提供 Keystore 生成和解析功能。
 * @author Sakura
 */
@Service
public interface KeystoreService {


    String generateKeystoreAndUpdateUser(String email, String password) throws Exception;

    String loadKeyPairFromKeystore(MultipartFile file, String password, String blockchainAddress) throws IOException;

}
