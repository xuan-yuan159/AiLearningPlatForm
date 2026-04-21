package org.xuanyuan.upload.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.service.UploadProgressSessionService;
import org.xuanyuan.upload.service.UploadTaskService;

@Component
@RequiredArgsConstructor
public class UploadProgressWebSocketHandler extends TextWebSocketHandler {

    private final UploadProgressSessionService uploadProgressSessionService;
    private final UploadTaskService uploadTaskService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String taskId = resolveTaskId(session);
        Long teacherId = resolveTeacherId(session);
        uploadProgressSessionService.register(taskId, teacherId, session);
        UploadTaskInfo taskInfo = uploadTaskService.prepareTask(taskId, teacherId);
        session.sendMessage(new TextMessage(com.alibaba.fastjson2.JSON.toJSONString(taskInfo)));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        uploadProgressSessionService.unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        uploadProgressSessionService.unregister(session);
    }

    private String resolveTaskId(WebSocketSession session) {
        String query = session.getUri() == null ? null : session.getUri().getQuery();
        if (!StringUtils.hasText(query)) {
            throw new BaseException(400, "uploadTaskId 不能为空");
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "uploadTaskId".equals(parts[0]) && StringUtils.hasText(parts[1])) {
                return normalizeTaskId(parts[1]);
            }
        }
        throw new BaseException(400, "uploadTaskId 不能为空");
    }

    private Long resolveTeacherId(WebSocketSession session) {
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new BaseException(401, "未登录，无法建立上传进度连接");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new BaseException(401, "用户信息不合法");
        }
    }

    private String normalizeTaskId(String taskId) {
        String candidate = taskId.trim();
        if (!candidate.contains(",")) {
            return candidate;
        }
        for (String part : candidate.split(",")) {
            if (StringUtils.hasText(part)) {
                return part.trim();
            }
        }
        throw new BaseException(400, "uploadTaskId 不能为空");
    }
}
