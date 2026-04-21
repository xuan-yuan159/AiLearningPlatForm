package org.xuanyuan.upload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.upload.dto.UploadProgressRequest;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskCreateRequest;
import org.xuanyuan.upload.dto.UploadTaskCreateResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.service.OssUploadService;
import org.xuanyuan.upload.service.UploadTaskService;

import java.util.List;

@Tag(name = "上传管理", description = "资源上传、进度查询与清理")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssUploadService ossUploadService;
    private final UploadTaskService uploadTaskService;

    @Operation(summary = "创建上传任务", description = "兼容旧流程，前端可先创建任务后再上传")
    @PostMapping("/tasks")
    public Result<UploadTaskCreateResult> createTask(@RequestBody UploadTaskCreateRequest request) {
        Long teacherId = UserContext.getUserId();
        return Result.success(uploadTaskService.createTask(request, teacherId));
    }

    @Deprecated
    @Operation(summary = "上报上传进度", description = "兼容旧流程，新流程改为后端自动采集并通过 WebSocket 推送")
    @PatchMapping("/tasks/{taskId}/progress")
    public Result<UploadTaskInfo> reportProgress(
            @Parameter(description = "上传任务 ID", required = true) @PathVariable("taskId") String taskId,
            @RequestBody UploadProgressRequest request) {
        Long teacherId = UserContext.getUserId();
        return Result.success(uploadTaskService.reportProgress(taskId, request, teacherId));
    }

    @Operation(summary = "查询上传任务", description = "查询单个上传任务的当前状态")
    @GetMapping("/tasks/{taskId}")
    public Result<UploadTaskInfo> getTask(
            @Parameter(description = "上传任务 ID", required = true) @PathVariable("taskId") String taskId) {
        Long teacherId = UserContext.getUserId();
        return Result.success(uploadTaskService.getTask(taskId, teacherId));
    }

    @Operation(summary = "查询最近上传任务", description = "返回当前教师最近的上传任务列表")
    @GetMapping("/tasks/recent")
    public Result<List<UploadTaskInfo>> listRecentTasks() {
        Long teacherId = UserContext.getUserId();
        return Result.success(uploadTaskService.listRecentTasks(teacherId));
    }

    @Operation(summary = "取消上传任务", description = "标记取消任务；如已写入资源，则一并清理资源")
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<UploadTaskInfo> cancelTask(
            @Parameter(description = "上传任务 ID", required = true) @PathVariable("taskId") String taskId) {
        Long teacherId = UserContext.getUserId();
        UploadTaskInfo task = uploadTaskService.getTask(taskId, teacherId);
        if (task.getResourceId() != null) {
            ossUploadService.deleteUploadedResource(task.getResourceId(), teacherId);
        }
        return Result.success(uploadTaskService.markCanceled(taskId, teacherId));
    }

    @Operation(summary = "统一资源上传", description = "前端生成 uploadTaskId 后，上传文件并由后端实时推送进度")
    @PostMapping("/resources")
    public Result<UploadTaskInfo> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam("resourceType") String resourceType,
            @RequestParam("uploadTaskId") String uploadTaskId) {
        Long teacherId = UserContext.getUserId();
        return Result.success(ossUploadService.uploadResource(file, courseId, title, resourceType, teacherId, uploadTaskId));
    }

    @Operation(summary = "视频上传", description = "兼容旧接口，内部复用统一上传逻辑")
    @PostMapping("/video")
    public Result<UploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "uploadTaskId", required = false) String uploadTaskId) {
        Long teacherId = UserContext.getUserId();
        return Result.success(ossUploadService.uploadVideo(file, courseId, title, teacherId, uploadTaskId));
    }

    @Operation(summary = "图片上传", description = "兼容旧接口，内部复用统一上传逻辑")
    @PostMapping("/image")
    public Result<UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "uploadTaskId", required = false) String uploadTaskId) {
        Long teacherId = UserContext.getUserId();
        return Result.success(ossUploadService.uploadImage(file, courseId, title, teacherId, uploadTaskId));
    }

    @Operation(summary = "删除已上传资源", description = "取消上传或手动清理时，删除 OSS 对象并移除 resource 记录")
    @DeleteMapping("/resource/{resourceId}")
    public Result<Void> deleteUploadedResource(
            @Parameter(description = "资源 ID", required = true) @PathVariable("resourceId") Long resourceId) {
        Long teacherId = UserContext.getUserId();
        ossUploadService.deleteUploadedResource(resourceId, teacherId);
        return Result.success();
    }
}
