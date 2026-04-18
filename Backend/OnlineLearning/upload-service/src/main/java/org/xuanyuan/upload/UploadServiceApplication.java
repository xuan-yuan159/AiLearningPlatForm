package org.xuanyuan.upload;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"org.xuanyuan.common", "org.xuanyuan.upload"})
@EnableDiscoveryClient
@MapperScan("org.xuanyuan.upload.mapper")
@ConfigurationPropertiesScan("org.xuanyuan.upload.config")
public class UploadServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UploadServiceApplication.class, args);
    }
}
