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
import org.xuanyuan.common.util.RoleUtils;
import org.xuanyuan.upload.dto.UploadProgressRequest;
import org.xuanyuan.upload.dto.UploadResult;
import org.xuanyuan.upload.dto.UploadTaskCreateRequest;
import org.xuanyuan.upload.dto.UploadTaskCreateResult;
import org.xuanyuan.upload.dto.UploadTaskInfo;
import org.xuanyuan.upload.service.OssUploadService;
import org.xuanyuan.upload.service.UploadTaskService;

import java.util.List;

@Tag(name = "上传管理", description = "教师上传接口与进度管理")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final OssUploadService ossUploadService;
    private final UploadTaskService uploadTaskService;

    /**
     * 创建上传任务（教师）
     */
    @Operation(summary = "创建上传任务", description = "上传前先创建任务")
    @PostMapping("/tasks")
    public Result<UploadTaskCreateResult> createTask(@RequestBody UploadTaskCreateRequest request) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(uploadTaskService.createTask(request, teacherId));
    }

    /**
     * 兼容旧流程的上报进度接口（教师）
     */
    @Deprecated
    @Operation(summary = "上报上传进度（兼容）", description = "兼容旧流程的进度上报接口")
    @PatchMapping("/tasks/{taskId}/progress")
    public Result<UploadTaskInfo> reportProgress(
            @Parameter(description = "任务ID", required = true) @PathVariable("taskId") String taskId,
            @RequestBody UploadProgressRequest request) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(uploadTaskService.reportProgress(taskId, request, teacherId));
    }

    /**
     * 查询单个上传任务（教师）
     */
    @Operation(summary = "查询上传任务", description = "查询单个任务状态")
    @GetMapping("/tasks/{taskId}")
    public Result<UploadTaskInfo> getTask(
            @Parameter(description = "任务ID", required = true) @PathVariable("taskId") String taskId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(uploadTaskService.getTask(taskId, teacherId));
    }

    /**
     * 查询最近上传任务（教师）
     */
    @Operation(summary = "查询最近上传任务", description = "查询当前教师最近上传任务列表")
    @GetMapping("/tasks/recent")
    public Result<List<UploadTaskInfo>> listRecentTasks() {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(uploadTaskService.listRecentTasks(teacherId));
    }

    /**
     * 取消上传任务（教师）
     */
    @Operation(summary = "取消上传任务", description = "取消上传并在需要时清理资源")
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<UploadTaskInfo> cancelTask(
            @Parameter(description = "任务ID", required = true) @PathVariable("taskId") String taskId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        UploadTaskInfo task = uploadTaskService.getTask(taskId, teacherId);
        if (task.getResourceId() != null) {
            ossUploadService.deleteUploadedResource(task.getResourceId(), teacherId); // 任务已落库时同步清理资源
        }
        return Result.success(uploadTaskService.markCanceled(taskId, teacherId));
    }

    /**
     * 统一资源上传（教师）
     */
    @Operation(summary = "统一资源上传", description = "统一处理资源上传")
    @PostMapping("/resources")
    public Result<UploadTaskInfo> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam("resourceType") String resourceType,
            @RequestParam("uploadTaskId") String uploadTaskId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(ossUploadService.uploadResource(file, courseId, title, resourceType, teacherId, uploadTaskId));
    }

    /**
     * 视频上传兼容接口（教师）
     */
    @Operation(summary = "上传视频（兼容）", description = "兼容旧前端的视频上传接口")
    @PostMapping("/video")
    public Result<UploadResult> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "uploadTaskId", required = false) String uploadTaskId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(ossUploadService.uploadVideo(file, courseId, title, teacherId, uploadTaskId));
    }

    /**
     * 图片上传兼容接口（教师）
     */
    @Operation(summary = "上传图片（兼容）", description = "兼容旧前端的图片上传接口")
    @PostMapping("/image")
    public Result<UploadResult> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam("title") String title,
            @RequestParam(value = "uploadTaskId", required = false) String uploadTaskId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(ossUploadService.uploadImage(file, courseId, title, teacherId, uploadTaskId));
    }

    /**
     * 删除已上传资源（教师）
     */
    @Operation(summary = "删除已上传资源", description = "删除 OSS 对象与数据库记录")
    @DeleteMapping("/resource/{resourceId}")
    public Result<Void> deleteUploadedResource(
            @Parameter(description = "资源ID", required = true) @PathVariable("resourceId") Long resourceId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        ossUploadService.deleteUploadedResource(resourceId, teacherId);
        return Result.success();
    }
}
