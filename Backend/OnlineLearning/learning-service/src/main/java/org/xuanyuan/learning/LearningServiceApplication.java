package org.xuanyuan.learning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"org.xuanyuan.common", "org.xuanyuan.learning"})
@EnableDiscoveryClient
@MapperScan("org.xuanyuan.learning.mapper")
public class LearningServiceApplication {

    /**
     * 学习服务启动入口
     */
    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }
}
