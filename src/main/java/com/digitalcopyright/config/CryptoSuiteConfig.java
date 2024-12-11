package com.digitalcopyright.config;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类：将 CryptoSuite 注册为 Spring 的 Bean。
 * @author Sakura
 */
@Configuration
public class CryptoSuiteConfig {

    private final Client client;

    public CryptoSuiteConfig(Client client) {
        this.client = client;
    }

    @Bean
    public CryptoSuite cryptoSuite() {
        return client.getCryptoSuite();
    }
}
