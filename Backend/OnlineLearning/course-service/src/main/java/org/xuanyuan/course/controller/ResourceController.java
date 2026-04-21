package org.xuanyuan.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.common.util.RoleUtils;
import org.xuanyuan.course.service.ResourceService;

@Tag(name = "资源管理", description = "资源绑定与资料下载接口")
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    /**
     * 资料打包下载（TODO）
     */
    @Operation(summary = "资料打包下载（TODO）", description = "资料批量打包下载功能暂未实现")
    @GetMapping("/download/materials/{courseId}")
    public Result<Void> downloadMaterials(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        resourceService.downloadMaterials();
        return Result.success();
    }

    /**
     * 绑定资源到章节（教师）
     */
    @Operation(summary = "绑定资源到章节", description = "教师将已上传资源绑定到指定章节")
    @PostMapping("/{id}/bind/{chapterId}")
    public Result<Void> bindResource(
            @Parameter(description = "资源ID", required = true) @PathVariable("id") Long id,
            @Parameter(description = "章节ID", required = true) @PathVariable("chapterId") Long chapterId) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        resourceService.bindResource(teacherId, chapterId, id);
        return Result.success();
    }
}
