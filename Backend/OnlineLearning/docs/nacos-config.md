# Nacos 配置数据汇总

本项目使用 Nacos 作为配置中心，以下是项目中使用的所有配置数据（Data Id）。

## 1. MySQL 配置 (Data Id: application-mysql.yml)
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${MYSQL_URL:jdbc:mysql://123.57.31.8:3306/AiLearningPlatform?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true}
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PWD:ENC(XuanYuan@159)}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 30000
      connection-timeout: 30000
      max-lifetime: 1800000
```

## 2. Redis 配置 (Data Id: application-redis.yml)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:123.57.31.8}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PWD:ENC(XuanYuan@159)}
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
```

## 3. RabbitMQ 配置 (Data Id: application-rabbitmq.yml)
```yaml
spring:
  rabbitmq:
    host: ${RABBIT_HOST:123.57.31.8}
    port: ${RABBIT_PORT:5672}
    username: ${RABBIT_USER:guest}
    password: ${RABBIT_PWD:ENC(guest)}
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
```

## 4. OSS 配置 (Data Id: application-oss.yml)
```yaml
aliyun:
  oss:
    endpoint: ${ALIYUN_OSS_ENDPOINT:oss-cn-beijing.aliyuncs.com}
    bucket-name: ${ALIYUN_OSS_BUCKET:your-bucket-name}
    access-key-id: ${ALIYUN_OSS_ACCESS_KEY_ID:}
    access-key-secret: ${ALIYUN_OSS_ACCESS_KEY_SECRET:}
```

> ⚠️ 注意：不要把 `ENC(your-access-key-id)` 这类占位文案直接放到 Nacos。  
> `ENC(...)` 必须是通过 Jasypt 生成的真实密文，否则会在启动时触发属性绑定失败。  
> 开发环境可直接填明文；生产环境请填真实 `ENC(密文)`。
