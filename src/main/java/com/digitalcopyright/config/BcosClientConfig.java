package com.digitalcopyright.config;

import cn.hutool.core.io.resource.ClassPathResource;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Sakura
 */
@Configuration
public class BcosClientConfig {
    @Bean
    public Client client() throws Exception {
        try {
            // 使用 ClassPathResource 加载配置文件
            ClassPathResource classPathResource = new ClassPathResource("config-example.toml");
            // 如果文件不存在，这里会抛异常
            File configFile = classPathResource.getFile();

            // 初始化 BcosSDK
            BcosSDK bcosSDK = BcosSDK.build(configFile.getAbsolutePath());

            return bcosSDK.getClient(1);
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败，请检查路径是否正确：config-example.toml", e);
        }
    }
}
