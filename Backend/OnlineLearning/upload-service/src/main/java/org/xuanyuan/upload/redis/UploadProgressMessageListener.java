package org.xuanyuan.upload.redis;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.service.UploadProgressSessionService;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class UploadProgressMessageListener implements MessageListener {

    private final UploadProgressSessionService uploadProgressSessionService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        UploadTaskInfo taskInfo = JSON.parseObject(body, UploadTaskInfo.class);
        uploadProgressSessionService.sendToLocal(taskInfo);
    }
}
