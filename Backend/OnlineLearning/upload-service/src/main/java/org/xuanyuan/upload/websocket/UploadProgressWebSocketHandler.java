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

    /**
     * 建立 WebSocket 连接并下发任务快照
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String taskId = resolveTaskId(session);
        Long teacherId = resolveTeacherId(session);
        uploadProgressSessionService.register(taskId, teacherId, session);
        UploadTaskInfo taskInfo = uploadTaskService.prepareTask(taskId, teacherId);
        session.sendMessage(new TextMessage(com.alibaba.fastjson2.JSON.toJSONString(taskInfo)));
    }

    /**
     * 连接异常时清理会话
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        uploadProgressSessionService.unregister(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    /**
     * 连接关闭时清理会话
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        uploadProgressSessionService.unregister(session);
    }

    /**
     * 解析上传任务ID
     */
    private String resolveTaskId(WebSocketSession session) {
        String query = session.getUri() == null ? null : session.getUri().getQuery();
        if (!StringUtils.hasText(query)) {
            throw new BaseException(400, "uploadTaskId is required");
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "uploadTaskId".equals(parts[0]) && StringUtils.hasText(parts[1])) {
                return normalizeTaskId(parts[1]);
            }
        }
        throw new BaseException(400, "uploadTaskId is required");
    }

    /**
     * 解析教师ID并校验角色
     */
    private Long resolveTeacherId(WebSocketSession session) {
        String role = session.getHandshakeHeaders().getFirst("X-User-Role");
        if (!StringUtils.hasText(role) || !"TEACHER".equalsIgnoreCase(role)) {
            throw new BaseException(403, "Only teacher can subscribe upload progress");
        }
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new BaseException(401, "Unauthorized user");
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            throw new BaseException(401, "Invalid user id");
        }
    }

    /**
     * 规范化任务ID
     */
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
        throw new BaseException(400, "uploadTaskId is required");
    }
}
