package org.xuanyuan.upload.service;

import org.springframework.web.multipart.MultipartFile;
import org.xuanyuan.upload.dto.UploadResult;

public interface OssUploadService {

    UploadResult uploadVideo(MultipartFile file, Long courseId, String title, Long teacherId);

    UploadResult uploadImage(MultipartFile file, Long courseId, String title, Long teacherId);
}
