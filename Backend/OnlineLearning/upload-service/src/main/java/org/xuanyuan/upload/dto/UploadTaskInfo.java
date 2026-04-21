package org.xuanyuan.upload.dto;

import lombok.Data;

/**
 * 上传任务状态。
 *
 * <p>该对象直接序列化到 Redis。字段保持扁平，方便前端直接展示。</p>
 */
@Data
public class UploadTaskInfo {

    private String taskId;

    private Long teacherId;

    private Long courseId;

    private String title;

    private String resourceType;

    private String fileName;

    private Long fileSize;

    private String status;

    private String stage;

    private Integer percent;

    private Long loadedBytes;

    private Long totalBytes;

    private Long resourceId;

    private String url;

    private String objectKey;

    private String errorMessage;

    private String createdAt;

    private String updatedAt;
}
