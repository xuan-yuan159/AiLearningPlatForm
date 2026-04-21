package org.xuanyuan.upload.service;

import org.springframework.web.socket.WebSocketSession;
import org.xuanyuan.upload.dto.UploadTaskInfo;

public interface UploadProgressSessionService {

    void register(String uploadTaskId, Long teacherId, WebSocketSession session);

    void unregister(WebSocketSession session);

    void sendToLocal(UploadTaskInfo taskInfo);
}
