package org.xuanyuan.upload.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.upload.constant.UploadTaskStatus;
import org.xuanyuan.upload.dto.UploadProgressRequest;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskCreateRequest;
import org.xuanyuan.upload.dto.UploadTaskCreateResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.entity.Course;
import org.xuanyuan.upload.mapper.CourseMapper;
import org.xuanyuan.upload.service.UploadProgressSessionService;
import org.xuanyuan.upload.service.UploadTaskService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadTaskServiceImpl implements UploadTaskService {

    public static final String PROGRESS_CHANNEL = "upload:progress";

    private static final String TASK_KEY_PREFIX = "upload:task:";
    private static final String TEACHER_TASK_LIST_PREFIX = "upload:teacher:";
    private static final long WORKING_TTL_HOURS = 2;
    private static final long FINISHED_TTL_HOURS = 24;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Set<String> ALLOWED_TYPES = Set.of("video", "image");

    private final StringRedisTemplate redisTemplate;
    private final CourseMapper courseMapper;
    private final UploadProgressSessionService uploadProgressSessionService;

    @Override
    public UploadTaskCreateResult createTask(UploadTaskCreateRequest request, Long teacherId) {
        validateCreateRequest(request);
        validateCourseOwnership(request.getCourseId(), teacherId);

        String taskId = UUID.randomUUID().toString().replace("-", "");
        UploadTaskInfo task = prepareTask(taskId, teacherId);
        task.setCourseId(request.getCourseId());
        task.setTitle(request.getTitle().trim());
        task.setResourceType(normalizeResourceType(request.getResourceType()));
        task.setFileName(request.getFileName());
        task.setFileSize(request.getFileSize());
        task.setTotalBytes(request.getFileSize());
        task.setStage("等待上传");
        saveWorkingTask(task);
        publish(task);
        return new UploadTaskCreateResult(taskId);
    }

    @Override
    public UploadTaskInfo prepareTask(String taskId, Long teacherId) {
        String normalizedTaskId = normalizeTaskId(taskId);
        validateTeacherId(teacherId);

        UploadTaskInfo task = loadTask(normalizedTaskId);
        if (task == null) {
            String now = nowText();
            task = new UploadTaskInfo();
            task.setTaskId(normalizedTaskId);
            task.setTeacherId(teacherId);
            task.setStatus(UploadTaskStatus.CREATED);
            task.setStage("等待上传");
            task.setPercent(0);
            task.setLoadedBytes(0L);
            task.setTotalBytes(0L);
            task.setCreatedAt(now);
            task.setUpdatedAt(now);
            saveWorkingTask(task);
            rememberRecentTask(teacherId, normalizedTaskId);
            publish(task);
            return task;
        }

        assertTeacher(task, teacherId);
        return task;
    }

    @Override
    public UploadTaskInfo bindTaskMetadata(String taskId, Long teacherId, Long courseId, String title,
                                           String resourceType, String fileName, Long fileSize) {
        UploadTaskInfo task = prepareTask(normalizeTaskId(taskId), teacherId);
        if (isFinished(task.getStatus())) {
            return task;
        }

        task.setCourseId(courseId);
        task.setTitle(StringUtils.hasText(title) ? title.trim() : title);
        task.setResourceType(normalizeResourceType(resourceType));
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        if (task.getTotalBytes() == null || task.getTotalBytes() <= 0) {
            task.setTotalBytes(fileSize);
        }
        task.setUpdatedAt(nowText());
        saveWorkingTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo recordReceivingProgress(String taskId, Long teacherId, Long loadedBytes, Long totalBytes) {
        String normalizedTaskId = normalizeTaskIdOrNull(taskId);
        if (!StringUtils.hasText(normalizedTaskId) || teacherId == null) {
            return null;
        }

        UploadTaskInfo task = prepareTask(normalizedTaskId, teacherId);
        if (isFinished(task.getStatus())) {
            return task;
        }

        long loaded = Math.max(0L, nonNullLong(loadedBytes));
        long total = Math.max(0L, nonNullLong(totalBytes));
        int percent = total > 0 ? (int) Math.min(90L, loaded * 90L / total) : 0;

        task.setStatus(UploadTaskStatus.UPLOADING);
        task.setStage("文件接收中");
        task.setLoadedBytes(loaded);
        task.setTotalBytes(total);
        task.setPercent(clamp(percent, 0, 90));
        task.setUpdatedAt(nowText());
        saveWorkingTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo reportProgress(String taskId, UploadProgressRequest request, Long teacherId) {
        UploadTaskInfo task = getTask(normalizeTaskId(taskId), teacherId);
        if (isFinished(task.getStatus())) {
            return task;
        }

        task.setStatus(UploadTaskStatus.UPLOADING);
        task.setStage("文件上传中");
        task.setLoadedBytes(nonNullLong(request.getLoadedBytes()));
        task.setTotalBytes(nonNullLong(request.getTotalBytes()));
        task.setPercent(clamp(nonNullInt(request.getPercent()), 0, 90));
        task.setUpdatedAt(nowText());
        saveWorkingTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo getTask(String taskId, Long teacherId) {
        UploadTaskInfo task = loadTask(normalizeTaskId(taskId));
        if (task == null) {
            throw new BaseException(404, "上传任务不存在或已过期");
        }
        assertTeacher(task, teacherId);
        return task;
    }

    @Override
    public List<UploadTaskInfo> listRecentTasks(Long teacherId) {
        validateTeacherId(teacherId);
        List<String> taskIds = redisTemplate.opsForList().range(teacherListKey(teacherId), 0, 19);
        List<UploadTaskInfo> result = new ArrayList<>();
        if (taskIds == null) {
            return result;
        }
        for (String taskId : taskIds) {
            String normalizedTaskId = normalizeTaskIdOrNull(taskId);
            if (!StringUtils.hasText(normalizedTaskId)) {
                continue;
            }
            UploadTaskInfo task = loadTask(normalizedTaskId);
            if (task != null && teacherId.equals(task.getTeacherId())) {
                result.add(task);
            }
        }
        return result;
    }

    @Override
    public UploadTaskInfo markStage(String taskId, String status, String stage, Integer percent) {
        String normalizedTaskId = normalizeTaskIdOrNull(taskId);
        if (!StringUtils.hasText(normalizedTaskId)) {
            return null;
        }
        UploadTaskInfo task = loadTask(normalizedTaskId);
        if (task == null || isFinished(task.getStatus())) {
            return task;
        }
        task.setStatus(status);
        task.setStage(stage);
        task.setPercent(clamp(nonNullInt(percent), 0, 99));
        task.setUpdatedAt(nowText());
        saveWorkingTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo markSuccess(String taskId, UploadResult result) {
        String normalizedTaskId = normalizeTaskIdOrNull(taskId);
        if (!StringUtils.hasText(normalizedTaskId)) {
            return null;
        }
        UploadTaskInfo task = loadTask(normalizedTaskId);
        if (task == null) {
            return null;
        }
        task.setStatus(UploadTaskStatus.SUCCESS);
        task.setStage("上传完成");
        task.setPercent(100);
        task.setResourceId(result.getResourceId());
        task.setUrl(result.getUrl());
        task.setObjectKey(result.getObjectKey());
        task.setErrorMessage(null);
        task.setUpdatedAt(nowText());
        saveFinishedTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo markError(String taskId, String errorMessage) {
        String normalizedTaskId = normalizeTaskIdOrNull(taskId);
        if (!StringUtils.hasText(normalizedTaskId)) {
            return null;
        }
        UploadTaskInfo task = loadTask(normalizedTaskId);
        if (task == null) {
            return null;
        }
        task.setStatus(UploadTaskStatus.ERROR);
        task.setStage("上传失败");
        task.setErrorMessage(errorMessage);
        task.setUpdatedAt(nowText());
        saveFinishedTask(task);
        publish(task);
        return task;
    }

    @Override
    public UploadTaskInfo markCanceled(String taskId, Long teacherId) {
        UploadTaskInfo task = getTask(normalizeTaskId(taskId), teacherId);
        task.setStatus(UploadTaskStatus.CANCELED);
        task.setStage("已取消");
        task.setErrorMessage(null);
        task.setUpdatedAt(nowText());
        saveFinishedTask(task);
        publish(task);
        return task;
    }

    private void validateCreateRequest(UploadTaskCreateRequest request) {
        if (request == null) {
            throw new BaseException(400, "上传任务参数不能为空");
        }
        if (request.getCourseId() == null || request.getCourseId() <= 0) {
            throw new BaseException(400, "courseId 参数不合法");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BaseException(400, "资源标题不能为空");
        }
        normalizeResourceType(request.getResourceType());
    }

    private void validateCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || Integer.valueOf(3).equals(course.getStatus())) {
            throw new BaseException(400, "课程不存在或已删除");
        }
        if (teacherId == null || !course.getTeacherId().equals(teacherId)) {
            throw new BaseException(403, "无权限操作非本人课程资源");
        }
    }

    private UploadTaskInfo loadTask(String taskId) {
        String normalizedTaskId = normalizeTaskId(taskId);
        String json = redisTemplate.opsForValue().get(taskKey(normalizedTaskId));
        if (!StringUtils.hasText(json)) {
            return null;
        }
        return JSON.parseObject(json, UploadTaskInfo.class);
    }

    private void saveWorkingTask(UploadTaskInfo task) {
        redisTemplate.opsForValue().set(taskKey(normalizeTaskId(task.getTaskId())), JSON.toJSONString(task), WORKING_TTL_HOURS, TimeUnit.HOURS);
    }

    private void saveFinishedTask(UploadTaskInfo task) {
        redisTemplate.opsForValue().set(taskKey(normalizeTaskId(task.getTaskId())), JSON.toJSONString(task), FINISHED_TTL_HOURS, TimeUnit.HOURS);
    }

    private void publish(UploadTaskInfo task) {
        if (task == null || !StringUtils.hasText(task.getTaskId())) {
            return;
        }
        uploadProgressSessionService.sendToLocal(task);
        redisTemplate.convertAndSend(PROGRESS_CHANNEL, JSON.toJSONString(task));
        log.info("Published upload progress: taskId={}, status={}, stage={}, percent={}", task.getTaskId(), task.getStatus(), task.getStage(), task.getPercent());
    }

    private void rememberRecentTask(Long teacherId, String taskId) {
        String key = teacherListKey(teacherId);
        String normalizedTaskId = normalizeTaskId(taskId);
        redisTemplate.opsForList().remove(key, 0, normalizedTaskId);
        redisTemplate.opsForList().leftPush(key, normalizedTaskId);
        redisTemplate.opsForList().trim(key, 0, 19);
        redisTemplate.expire(key, FINISHED_TTL_HOURS, TimeUnit.HOURS);
    }

    private boolean isFinished(String status) {
        return UploadTaskStatus.SUCCESS.equals(status)
                || UploadTaskStatus.ERROR.equals(status)
                || UploadTaskStatus.CANCELED.equals(status);
    }

    private void assertTeacher(UploadTaskInfo task, Long teacherId) {
        if (teacherId == null || !teacherId.equals(task.getTeacherId())) {
            throw new BaseException(403, "无权限查看该上传任务");
        }
    }

    private void validateTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new BaseException(401, "未登录，无法操作上传任务");
        }
    }

    private void validateTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            throw new BaseException(400, "taskId 不能为空");
        }
    }

    private String normalizeTaskId(String taskId) {
        validateTaskId(taskId);
        String candidate = taskId.trim();
        if (!candidate.contains(",")) {
            return candidate;
        }

        for (String part : candidate.split(",")) {
            if (StringUtils.hasText(part)) {
                return part.trim();
            }
        }
        throw new BaseException(400, "taskId 不能为空");
    }

    private String normalizeTaskIdOrNull(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return null;
        }
        String candidate = taskId.trim();
        if (!candidate.contains(",")) {
            return candidate;
        }

        for (String part : candidate.split(",")) {
            if (StringUtils.hasText(part)) {
                return part.trim();
            }
        }
        return null;
    }

    private String normalizeResourceType(String resourceType) {
        if (!StringUtils.hasText(resourceType)) {
            throw new BaseException(400, "资源类型不能为空");
        }
        String normalized = resourceType.trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new BaseException(400, "资源类型仅支持 video 或 image");
        }
        return normalized;
    }

    private String taskKey(String taskId) {
        return TASK_KEY_PREFIX + taskId;
    }

    private String teacherListKey(Long teacherId) {
        return TEACHER_TASK_LIST_PREFIX + teacherId + ":tasks";
    }

    private String nowText() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    private long nonNullLong(Long value) {
        return value == null ? 0L : value;
    }

    private int nonNullInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
