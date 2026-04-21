package org.xuanyuan.upload.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.service.UploadProgressSessionService;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson2.JSON;

@Slf4j
@Service
public class UploadProgressSessionServiceImpl implements UploadProgressSessionService {

    private final Map<String, SessionHolder> sessionById = new ConcurrentHashMap<>();
    private final Map<String, Map<String, SessionHolder>> taskSessions = new ConcurrentHashMap<>();

    @Override
    public void register(String uploadTaskId, Long teacherId, WebSocketSession session) {
        SessionHolder holder = new SessionHolder(uploadTaskId, teacherId, session);
        sessionById.put(session.getId(), holder);
        taskSessions.computeIfAbsent(uploadTaskId, key -> new ConcurrentHashMap<>()).put(session.getId(), holder);
    }

    @Override
    public void unregister(WebSocketSession session) {
        if (session == null) {
            return;
        }
        SessionHolder holder = sessionById.remove(session.getId());
        if (holder == null) {
            return;
        }
        Map<String, SessionHolder> sessions = taskSessions.get(holder.uploadTaskId());
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                taskSessions.remove(holder.uploadTaskId(), sessions);
            }
        }
    }

    @Override
    public void sendToLocal(UploadTaskInfo taskInfo) {
        if (taskInfo == null || taskInfo.getTaskId() == null || taskInfo.getTeacherId() == null) {
            return;
        }
        Map<String, SessionHolder> sessions = taskSessions.get(taskInfo.getTaskId());
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload = JSON.toJSONString(taskInfo);
        for (SessionHolder holder : sessions.values()) {
            if (!Objects.equals(holder.teacherId(), taskInfo.getTeacherId())) {
                continue;
            }
            WebSocketSession session = holder.session();
            if (!session.isOpen()) {
                unregister(session);
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.warn("Failed to push upload progress, sessionId={}", session.getId(), e);
                unregister(session);
            }
        }
    }

    private record SessionHolder(String uploadTaskId, Long teacherId, WebSocketSession session) {
    }
}
