package org.xuanyuan.upload.constant;

/**
 * 上传任务状态常量。
 *
 * <p>这里不使用复杂枚举，是为了让前后端展示和 Redis 存储都更直观。
 * 毕设项目中只需要覆盖创建、上传中、后端处理、成功、失败、取消这几类状态。</p>
 */
public final class UploadTaskStatus {

    public static final String CREATED = "created";
    public static final String UPLOADING = "uploading";
    public static final String RECEIVED = "received";
    public static final String OSS_UPLOADING = "oss_uploading";
    public static final String PERSISTING = "persisting";
    public static final String SUCCESS = "success";
    public static final String ERROR = "error";
    public static final String CANCELED = "canceled";

    private UploadTaskStatus() {
    }
}
