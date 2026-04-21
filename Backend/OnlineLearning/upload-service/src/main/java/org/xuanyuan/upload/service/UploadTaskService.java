package org.xuanyuan.upload.service;

import org.xuanyuan.upload.dto.UploadProgressRequest;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskCreateRequest;
import org.xuanyuan.upload.dto.UploadTaskCreateResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;

import java.util.List;

/**
 * 上传任务进度服务。
 *
 * <p>只负责 Redis 中的任务状态，不负责真正保存文件。
 * 真正上传 OSS 和落库仍由 OssUploadService 完成。</p>
 */
public interface UploadTaskService {

    UploadTaskCreateResult createTask(UploadTaskCreateRequest request, Long teacherId);

    UploadTaskInfo prepareTask(String taskId, Long teacherId);

    UploadTaskInfo bindTaskMetadata(String taskId, Long teacherId, Long courseId, String title,
                                    String resourceType, String fileName, Long fileSize);

    UploadTaskInfo recordReceivingProgress(String taskId, Long teacherId, Long loadedBytes, Long totalBytes);

    UploadTaskInfo reportProgress(String taskId, UploadProgressRequest request, Long teacherId);

    UploadTaskInfo getTask(String taskId, Long teacherId);

    List<UploadTaskInfo> listRecentTasks(Long teacherId);

    UploadTaskInfo markStage(String taskId, String status, String stage, Integer percent);

    UploadTaskInfo markSuccess(String taskId, UploadResult result);

    UploadTaskInfo markError(String taskId, String errorMessage);

    UploadTaskInfo markCanceled(String taskId, Long teacherId);
}
