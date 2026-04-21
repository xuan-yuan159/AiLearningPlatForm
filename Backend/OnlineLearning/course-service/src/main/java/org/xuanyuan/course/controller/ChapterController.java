package org.xuanyuan.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.common.util.RoleUtils;
import org.xuanyuan.course.dto.ChapterSaveReq;
import org.xuanyuan.course.entity.Chapter;
import org.xuanyuan.course.service.ChapterService;

import java.util.List;

@Tag(name = "章节管理", description = "章节增删改查接口")
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    /**
     * 创建章节（教师）
     */
    @Operation(summary = "创建章节", description = "教师在课程下创建章节")
    @PostMapping
    public Result<Long> createChapter(@RequestBody @Valid ChapterSaveReq req) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(chapterService.createChapter(teacherId, req));
    }

    /**
     * 编辑章节（教师）
     */
    @Operation(summary = "编辑章节", description = "教师编辑章节信息")
    @PutMapping("/{id}")
    public Result<Void> updateChapter(
            @Parameter(description = "章节ID", required = true) @PathVariable("id") Long id,
            @RequestBody @Valid ChapterSaveReq req) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        chapterService.updateChapter(teacherId, id, req);
        return Result.success();
    }

    /**
     * 删除章节（教师）
     */
    @Operation(summary = "删除章节", description = "教师删除指定章节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteChapter(
            @Parameter(description = "章节ID", required = true) @PathVariable("id") Long id) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        chapterService.deleteChapter(teacherId, id);
        return Result.success();
    }

    /**
     * 查询章节列表（通用）
     */
    @Operation(summary = "查询章节列表", description = "按课程查询章节列表")
    @GetMapping("/list/{courseId}")
    public Result<List<Chapter>> getChapterList(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        return Result.success(chapterService.getChapterList(courseId));
    }
}
