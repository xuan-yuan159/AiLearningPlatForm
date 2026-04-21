package org.xuanyuan.upload.service;

import org.springframework.web.multipart.MultipartFile;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;

public interface OssUploadService {

    UploadTaskInfo uploadResource(MultipartFile file, Long courseId, String title, String resourceType,
                                  Long teacherId, String uploadTaskId);

    UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId);

    UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId, String uploadTaskId);

    UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId);

    UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId, String uploadTaskId);

    void deleteUploadedResource(Long resourceId, Long teacherId);
}
