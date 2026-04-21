package org.xuanyuan.upload.dto;

import lombok.Data;

/**
 * 前端上传进度上报请求。
 *
 * <p>浏览器 XHR 可以实时拿到“浏览器传到后端”的进度，
 * 前端定期把这个进度写到 Redis，页面再轮询 Redis 展示。</p>
 */
@Data
public class UploadProgressRequest {

    private Long loadedBytes;

    private Long totalBytes;

    private Integer percent;
}
