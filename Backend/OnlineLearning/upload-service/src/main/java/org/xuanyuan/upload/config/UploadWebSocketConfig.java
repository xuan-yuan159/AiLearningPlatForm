package org.xuanyuan.upload.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.xuanyuan.upload.websocket.UploadProgressWebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class UploadWebSocketConfig implements WebSocketConfigurer {

    private final UploadProgressWebSocketHandler uploadProgressWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(uploadProgressWebSocketHandler, "/upload/ws/progress")
                .setAllowedOriginPatterns("*");
    }
}
