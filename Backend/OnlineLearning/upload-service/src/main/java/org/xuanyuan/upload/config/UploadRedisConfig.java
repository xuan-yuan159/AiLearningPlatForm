package org.xuanyuan.upload.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.xuanyuan.upload.redis.UploadProgressMessageListener;
import org.xuanyuan.upload.service.impl.UploadTaskServiceImpl;

@Configuration
@RequiredArgsConstructor
public class UploadRedisConfig {

    private final UploadProgressMessageListener uploadProgressMessageListener;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(uploadProgressMessageListener, new ChannelTopic(UploadTaskServiceImpl.PROGRESS_CHANNEL));
        return container;
    }
}
