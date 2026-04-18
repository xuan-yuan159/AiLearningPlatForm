package org.xuanyuan.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.service.OssUploadService;

@Tag(name = "上传管理", description = "视频上传与图片上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssUploadService ossUploadService;

    @Operation(summary = "视频上传", description = "上传视频到 OSS 并写入 resource 表")
    @PostMapping("/video")
    public Result<UploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title) {
        Long teacherId = UserContext.getUserId();
        return Result.success(ossUploadService.uploadVideo(file, courseId, title, teacherId));
    }

    @Operation(summary = "图片上传", description = "上传图片到 OSS 并写入 resource 表")
    @PostMapping("/image")
    public Result<UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title) {
        Long teacherId = UserContext.getUserId();
        return Result.success(ossUploadService.uploadImage(file, courseId, title, teacherId));
    }
}
