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
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.entity.Course;
import org.xuanyuan.upload.entity.Resource;
import org.xuanyuan.upload.mapper.CourseMapper;
import org.xuanyuan.upload.mapper.ResourceMapper;
import org.xuanyuan.upload.service.OssUploadService;

import java.io.IOException;
import java.io.InputStream;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId) {
        validateCommonParams(file, courseId, title);
        validateCourseOwnership(courseId, teacherId);
        validateFileType(file, true);
        return uploadAndPersist(file, courseId, title, RESOURCE_TYPE_VIDEO, "videos");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId) {
        validateCommonParams(file, courseId, title);
        validateCourseOwnership(courseId, teacherId);
        validateFileType(file, false);
        return uploadAndPersist(file, courseId, title, RESOURCE_TYPE_IMAGE, "images");
    }

    private UploadResult uploadAndPersist(MultipartFile file, Long courseId, String title, Integer type, String typeDir) {
        ensureOssConfigured();

        String objectKey = buildObjectKey(file, typeDir);
        String url = uploadToOss(file, objectKey);

        try {
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
            return new UploadResult(resource.getId(), url, objectKey);
        } catch (Exception e) {
            deleteObjectQuietly(objectKey);
            throw new BaseException("上传成功但写入资源记录失败: " + e.getMessage());
        }
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

    private void validateCourseOwnership(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || Integer.valueOf(3).equals(course.getStatus())) {
            throw new BaseException(400, "课程不存在或已删除");
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

    private String uploadToOss(MultipartFile file, String objectKey) {
        OSS ossClient = null;
        try {
            ossClient = buildOssClient();
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(aliyunOssProperties.getBucketName(), objectKey, inputStream);
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

    private void ensureOssConfigured() {
        if (!StringUtils.hasText(aliyunOssProperties.getEndpoint())
                || !StringUtils.hasText(aliyunOssProperties.getBucketName())
                || !StringUtils.hasText(aliyunOssProperties.getAccessKeyId())
                || !StringUtils.hasText(aliyunOssProperties.getAccessKeySecret())) {
            throw new BaseException("OSS 配置不完整，请检查 Nacos 的 application-oss.yml");
        }
    }
}
