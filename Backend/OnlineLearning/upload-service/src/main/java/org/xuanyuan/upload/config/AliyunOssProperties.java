package org.xuanyuan.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {

    private String endpoint;

    private String bucketName;

    private String accessKeyId;

    private String accessKeySecret;
}
