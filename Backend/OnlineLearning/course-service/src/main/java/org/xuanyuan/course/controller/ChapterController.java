package org.xuanyuan.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.course.dto.ChapterSaveReq;
import org.xuanyuan.course.entity.Chapter;
import org.xuanyuan.course.service.ChapterService;

import java.util.List;

@Tag(name = "章节管理", description = "课程章节的增删改查")
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @Operation(summary = "创建章节", description = "在指定课程下创建新章节")
    @PostMapping
    public Result<Long> createChapter(@RequestBody @Valid ChapterSaveReq req) {
        Long teacherId = UserContext.getUserId();
        return Result.success(chapterService.createChapter(teacherId, req));
    }

    @Operation(summary = "编辑章节", description = "修改章节标题、简介、排序等")
    @PutMapping("/{id}")
    public Result<Void> updateChapter(
            @Parameter(description = "章节ID", required = true) @PathVariable("id") Long id,
            @RequestBody @Valid ChapterSaveReq req) {
        Long teacherId = UserContext.getUserId();
        chapterService.updateChapter(teacherId, id, req);
        return Result.success();
    }

    @Operation(summary = "删除章节", description = "删除指定的章节")
    @DeleteMapping("/{id}")
    public Result<Void> deleteChapter(
            @Parameter(description = "章节ID", required = true) @PathVariable("id") Long id) {
        Long teacherId = UserContext.getUserId();
        chapterService.deleteChapter(teacherId, id);
        return Result.success();
    }

    @Operation(summary = "获取章节列表", description = "查询指定课程的章节目录（按排序）")
    @GetMapping("/list/{courseId}")
    public Result<List<Chapter>> getChapterList(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        return Result.success(chapterService.getChapterList(courseId));
    }
}