package org.xuanyuan.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建上传任务响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadTaskCreateResult {

    private String taskId;
}
