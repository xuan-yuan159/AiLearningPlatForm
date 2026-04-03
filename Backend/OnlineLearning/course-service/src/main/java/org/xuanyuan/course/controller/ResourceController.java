package org.xuanyuan.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.course.service.ResourceService;

@Tag(name = "课程资源管理", description = "上传视频、资料、打包下载")
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "视频上传", description = "上传课程视频文件")
    @PostMapping("/video/upload")
    public Result<Void> uploadVideo() {
        // TODO 视频上传待接入 OSS
        resourceService.uploadVideo();
        return Result.success();
    }

    @Operation(summary = "图片上传", description = "上传封面或其他图片文件")
    @PostMapping("/image/upload")
    public Result<Void> uploadImage() {
        // TODO 图片上传待接入 OSS
        return Result.error(500, "图片上传待实现");
    }

    @Operation(summary = "资料打包下载", description = "批量下载课程所有资料")
    @GetMapping("/download/materials/{courseId}")
    public Result<Void> downloadMaterials(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        // TODO 课程资料打包下载
        resourceService.downloadMaterials();
        return Result.success();
    }

    @Operation(summary = "绑定资源到章节", description = "将上传好的视频或资料挂载到特定章节")
    @PostMapping("/{id}/bind/{chapterId}")
    public Result<Void> bindResource(
            @Parameter(description = "资源ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "章节ID", required = true) @PathVariable("chapterId") Long chapterId) {
        Long teacherId = UserContext.getUserId();
        resourceService.bindResource(teacherId, chapterId, id);
        return Result.success();
    }
}