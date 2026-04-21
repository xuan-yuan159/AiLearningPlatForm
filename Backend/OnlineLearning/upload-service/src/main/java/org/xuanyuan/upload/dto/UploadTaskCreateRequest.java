package org.xuanyuan.upload.dto;

import lombok.Data;

/**
 * 创建上传任务请求。
 *
 * <p>前端真正上传文件前先创建任务，后续就可以通过 taskId 上报进度、
 * 轮询状态和执行取消操作。</p>
 */
@Data
public class UploadTaskCreateRequest {

    private Long courseId;

    private String title;

    /**
     * 资源类型：video 或 image。
     */
    private String resourceType;

    private String fileName;

    private Long fileSize;
}
