package org.xuanyuan.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.course.service.ResourceService;

@Tag(name = "课程资源管理", description = "课程资源绑定与资料下载")
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @Operation(summary = "资料打包下载", description = "批量下载课程所有资料（TODO，暂未实现）")
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
