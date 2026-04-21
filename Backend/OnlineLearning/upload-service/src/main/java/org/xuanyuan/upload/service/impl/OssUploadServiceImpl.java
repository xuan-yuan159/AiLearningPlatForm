package org.xuanyuan.upload.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.upload.config.AliyunOssProperties;
import org.xuanyuan.upload.constant.UploadTaskStatus;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.entity.Course;
import org.xuanyuan.upload.entity.Resource;
import org.xuanyuan.upload.mapper.CourseMapper;
import org.xuanyuan.upload.mapper.ResourceMapper;
import org.xuanyuan.upload.service.OssUploadService;
import org.xuanyuan.upload.service.UploadTaskService;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssUploadServiceImpl implements OssUploadService {

    private static final Integer RESOURCE_TYPE_VIDEO = 1;
    private static final Integer RESOURCE_TYPE_IMAGE = 2;

    private static final Set<String> VIDEO_CONTENT_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/x-msvideo", "video/x-matroska", "video/webm", "video/mpeg"
    );
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm", "mpeg", "mpg");

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    private final AliyunOssProperties aliyunOssProperties;
    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final UploadTaskService uploadTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadTaskInfo uploadResource(MultipartFile file, Long courseId, String title, String resourceType,
                                         Long teacherId, String uploadTaskId) {
        boolean taskPrepared = false;
        if (StringUtils.hasText(uploadTaskId) && teacherId != null) {
            uploadTaskService.prepareTask(uploadTaskId, teacherId);
            taskPrepared = true;
        }
        try {
            validateCommonParams(file, courseId, title);
            UploadTarget uploadTarget = resolveUploadTarget(resourceType);
            validateCourseOwnershipForUpload(courseId, teacherId);
            validateFileType(file, uploadTarget.resourceTypeCode().equals(RESOURCE_TYPE_VIDEO));

            if (StringUtils.hasText(uploadTaskId) && teacherId != null) {
                uploadTaskService.bindTaskMetadata(
                        uploadTaskId,
                        teacherId,
                        courseId,
                        title,
                        uploadTarget.resourceTypeName(),
                        file.getOriginalFilename(),
                        file.getSize()
                );
            }

            UploadResult result = uploadAndPersist(
                    file,
                    courseId,
                    title,
                    uploadTarget.resourceTypeCode(),
                    uploadTarget.ossDir(),
                    uploadTaskId
            );
            if (!StringUtils.hasText(uploadTaskId)) {
                return buildStandaloneResult(result, courseId, title, uploadTarget.resourceTypeName(), file);
            }
            return uploadTaskService.getTask(uploadTaskId, teacherId);
        } catch (Exception e) {
            if (taskPrepared) {
                uploadTaskService.markError(uploadTaskId, e.getMessage());
            }
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId) {
        return uploadVideo(file, courseId, title, teacherId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId, String uploadTaskId) {
        return toUploadResult(uploadResource(file, courseId, title, "video", teacherId, uploadTaskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId) {
        return uploadImage(file, courseId, title, teacherId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId, String uploadTaskId) {
        return toUploadResult(uploadResource(file, courseId, title, "image", teacherId, uploadTaskId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUploadedResource(Long resourceId, Long teacherId) {
        if (resourceId == null || resourceId <= 0) {
            throw new BaseException(400, "resourceId 参数不合法");
        }

        Resource resource = resourceMapper.selectById(resourceId);
        if (resource == null) {
            return;
        }

        validateCourseOwnership(resource.getCourseId(), teacherId);

        String objectKey = resolveObjectKeyFromUrl(resource.getUrl());
        deleteObject(objectKey);
        resourceMapper.deleteById(resourceId);
    }

    private UploadResult uploadAndPersist(MultipartFile file, Long courseId, String title, Integer type,
                                          String typeDir, String uploadTaskId) {
        String objectKey = null;
        try {
            uploadTaskService.markStage(uploadTaskId, UploadTaskStatus.RECEIVED, "后端已接收文件", 90);
            ensureOssConfigured();

            objectKey = buildObjectKey(file, typeDir);
            String url = uploadToOss(file, objectKey, uploadTaskId);

            uploadTaskService.markStage(uploadTaskId, UploadTaskStatus.PERSISTING, "保存资源记录", 99);
            Resource resource = new Resource();
            resource.setCourseId(courseId);
            resource.setChapterId(null);
            resource.setType(type);
            resource.setTitle(title.trim());
            resource.setUrl(url);
            resource.setSizeBytes(file.getSize());
            resource.setDurationS(null);
            resource.setSortOrder(0);

            resourceMapper.insert(resource);
            UploadResult result = new UploadResult(resource.getId(), url, objectKey);
            uploadTaskService.markSuccess(uploadTaskId, result);
            return result;
        } catch (Exception e) {
            if (StringUtils.hasText(objectKey)) {
                deleteObjectQuietly(objectKey);
            }
            throw e;
        }
    }

    private UploadTarget resolveUploadTarget(String resourceType) {
        if (!StringUtils.hasText(resourceType)) {
            throw new BaseException(400, "resourceType 不能为空");
        }
        String normalized = resourceType.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "video" -> new UploadTarget(RESOURCE_TYPE_VIDEO, "video", "videos");
            case "image" -> new UploadTarget(RESOURCE_TYPE_IMAGE, "image", "images");
            default -> throw new BaseException(400, "resourceType 仅支持 video 或 image");
        };
    }

    private UploadTaskInfo buildStandaloneResult(UploadResult result, Long courseId, String title,
                                                 String resourceType, MultipartFile file) {
        UploadTaskInfo taskInfo = new UploadTaskInfo();
        taskInfo.setCourseId(courseId);
        taskInfo.setTitle(title.trim());
        taskInfo.setResourceType(resourceType);
        taskInfo.setFileName(file.getOriginalFilename());
        taskInfo.setFileSize(file.getSize());
        taskInfo.setStatus(UploadTaskStatus.SUCCESS);
        taskInfo.setStage("上传完成");
        taskInfo.setPercent(100);
        taskInfo.setLoadedBytes(file.getSize());
        taskInfo.setTotalBytes(file.getSize());
        taskInfo.setResourceId(result.getResourceId());
        taskInfo.setUrl(result.getUrl());
        taskInfo.setObjectKey(result.getObjectKey());
        return taskInfo;
    }

    private UploadResult toUploadResult(UploadTaskInfo taskInfo) {
        return new UploadResult(taskInfo.getResourceId(), taskInfo.getUrl(), taskInfo.getObjectKey());
    }

    private void validateCommonParams(MultipartFile file, Long courseId, String title) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(400, "上传文件不能为空");
        }
        if (courseId == null || courseId <= 0) {
            throw new BaseException(400, "courseId 参数不合法");
        }
        if (!StringUtils.hasText(title)) {
            throw new BaseException(400, "title 不能为空");
        }
    }

    private void validateCourseOwnershipForUpload(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || Integer.valueOf(3).equals(course.getStatus())) {
            throw new BaseException(400, "课程不存在或已删除");
        }
        if (teacherId == null || !course.getTeacherId().equals(teacherId)) {
            throw new BaseException(403, "无权限操作非本人课程资源");
        }
    }

    private void validateCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BaseException(400, "资源所属课程不存在");
        }
        if (teacherId == null || !course.getTeacherId().equals(teacherId)) {
            throw new BaseException(403, "无权限操作非本人课程资源");
        }
    }

    private void validateFileType(MultipartFile file, boolean video) {
        String contentType = file.getContentType();
        String extension = resolveExtension(file);

        if (video) {
            if (!VIDEO_EXTENSIONS.contains(extension) && !VIDEO_CONTENT_TYPES.contains(normalizeContentType(contentType))) {
                throw new BaseException(400, "仅支持常见视频格式文件");
            }
            return;
        }

        if (!IMAGE_EXTENSIONS.contains(extension) && !IMAGE_CONTENT_TYPES.contains(normalizeContentType(contentType))) {
            throw new BaseException(400, "仅支持常见图片格式文件");
        }
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT);
    }

    private String buildObjectKey(MultipartFile file, String typeDir) {
        LocalDate now = LocalDate.now();
        String extension = resolveExtension(file);
        return String.format("%s/%d/%02d/%02d/%s.%s",
                typeDir,
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                UUID.randomUUID().toString().replace("-", ""),
                extension
        );
    }

    private String resolveExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (StringUtils.hasText(extension)) {
            return extension.toLowerCase(Locale.ROOT);
        }

        String contentType = normalizeContentType(file.getContentType());
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/bmp" -> "bmp";
            case "video/mp4" -> "mp4";
            case "video/quicktime" -> "mov";
            case "video/x-msvideo" -> "avi";
            case "video/x-matroska" -> "mkv";
            case "video/webm" -> "webm";
            case "video/mpeg" -> "mpeg";
            default -> throw new BaseException(400, "无法识别文件扩展名");
        };
    }

    private String uploadToOss(MultipartFile file, String objectKey, String uploadTaskId) {
        OSS ossClient = null;
        try {
            ossClient = buildOssClient();
            try (InputStream inputStream = file.getInputStream();
                 InputStream progressInputStream = new OssProgressInputStream(inputStream, file.getSize(), uploadTaskId, uploadTaskService)) {
                uploadTaskService.markStage(uploadTaskId, UploadTaskStatus.OSS_UPLOADING, "上传到 OSS 中", 90);
                ossClient.putObject(aliyunOssProperties.getBucketName(), objectKey, progressInputStream);
            }
            return buildPublicUrl(objectKey);
        } catch (IOException e) {
            throw new BaseException("读取上传文件失败: " + e.getMessage());
        } catch (Exception e) {
            throw new BaseException("上传 OSS 失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void deleteObjectQuietly(String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = buildOssClient();
            ossClient.deleteObject(aliyunOssProperties.getBucketName(), objectKey);
        } catch (Exception ignored) {
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void deleteObject(String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = buildOssClient();
            ossClient.deleteObject(aliyunOssProperties.getBucketName(), objectKey);
        } catch (Exception e) {
            throw new BaseException("删除 OSS 资源失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private OSS buildOssClient() {
        return new OSSClientBuilder().build(
                normalizeEndpointForClient(aliyunOssProperties.getEndpoint()),
                aliyunOssProperties.getAccessKeyId(),
                aliyunOssProperties.getAccessKeySecret()
        );
    }

    private String buildPublicUrl(String objectKey) {
        String endpointHost = normalizeEndpointHost(aliyunOssProperties.getEndpoint());
        String bucketName = aliyunOssProperties.getBucketName().trim();
        String host = endpointHost.startsWith(bucketName + ".") ? endpointHost : bucketName + "." + endpointHost;
        return "https://" + host + "/" + objectKey;
    }

    private String normalizeEndpointForClient(String endpoint) {
        String trimmed = endpoint.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }

    private String normalizeEndpointHost(String endpoint) {
        String host = endpoint.trim();
        if (host.startsWith("http://")) {
            host = host.substring("http://".length());
        } else if (host.startsWith("https://")) {
            host = host.substring("https://".length());
        }
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    private String resolveObjectKeyFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new BaseException(400, "资源 URL 为空，无法定位 OSS 对象");
        }
        try {
            URI uri = URI.create(url.trim());
            String path = uri.getPath();
            if (!StringUtils.hasText(path) || "/".equals(path)) {
                throw new BaseException(400, "资源 URL 路径为空，无法定位 OSS 对象");
            }
            String objectKey = path.startsWith("/") ? path.substring(1) : path;
            if (!StringUtils.hasText(objectKey)) {
                throw new BaseException(400, "资源 URL 无有效对象 key");
            }
            return objectKey;
        } catch (IllegalArgumentException e) {
            throw new BaseException(400, "资源 URL 格式不合法，无法定位 OSS 对象");
        }
    }

    private void ensureOssConfigured() {
        if (!StringUtils.hasText(aliyunOssProperties.getEndpoint())
                || !StringUtils.hasText(aliyunOssProperties.getBucketName())
                || !StringUtils.hasText(aliyunOssProperties.getAccessKeyId())
                || !StringUtils.hasText(aliyunOssProperties.getAccessKeySecret())) {
            throw new BaseException("OSS 配置不完整，请检查 Nacos 的 application-oss.yml");
        }
    }

    private record UploadTarget(Integer resourceTypeCode, String resourceTypeName, String ossDir) {
    }

    private static class OssProgressInputStream extends FilterInputStream {

        private static final long REPORT_STEP_BYTES = 256 * 1024;

        private final long totalBytes;
        private final String uploadTaskId;
        private final UploadTaskService uploadTaskService;
        private long loadedBytes;
        private long lastReportedBytes;
        private int lastReportedPercent = 89;

        private OssProgressInputStream(InputStream in, long totalBytes, String uploadTaskId,
                                       UploadTaskService uploadTaskService) {
            super(in);
            this.totalBytes = Math.max(0L, totalBytes);
            this.uploadTaskId = uploadTaskId;
            this.uploadTaskService = uploadTaskService;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value != -1) {
                afterRead(1);
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = super.read(b, off, len);
            afterRead(count);
            return count;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int count = in.read(b);
            afterRead(count);
            return count;
        }

        private void afterRead(int count) {
            if (count <= 0 || !StringUtils.hasText(uploadTaskId)) {
                return;
            }
            loadedBytes += count;
            int percent = totalBytes > 0
                    ? (int) Math.min(98L, 90L + loadedBytes * 8L / totalBytes)
                    : 90;
            boolean shouldReport = loadedBytes - lastReportedBytes >= REPORT_STEP_BYTES
                    || percent != lastReportedPercent
                    || (totalBytes > 0 && loadedBytes >= totalBytes);
            if (!shouldReport) {
                return;
            }
            lastReportedBytes = loadedBytes;
            lastReportedPercent = percent;
            uploadTaskService.markStage(uploadTaskId, UploadTaskStatus.OSS_UPLOADING, "上传到 OSS 中", percent);
        }
    }
}
