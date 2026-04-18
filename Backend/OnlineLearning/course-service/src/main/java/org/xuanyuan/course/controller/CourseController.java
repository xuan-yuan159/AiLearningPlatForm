package org.xuanyuan.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.course.dto.CourseSaveReq;
import org.xuanyuan.course.entity.Course;
import org.xuanyuan.course.service.CourseService;

@Tag(name = "课程管理", description = "教师课程管理、发布、上下架")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "创建课程", description = "创建课程草稿")
    @PostMapping
    public Result<Long> createCourse(@RequestBody @Valid CourseSaveReq req) {
        Long teacherId = UserContext.getUserId();
        // TODO 富文本编辑器处理课程详情的图文混排
        return Result.success(courseService.createCourse(teacherId, req));
    }

    @Operation(summary = "编辑课程", description = "修改课程信息")
    @PutMapping("/{id}")
    public Result<Void> updateCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id,
            @RequestBody @Valid CourseSaveReq req) {
        Long teacherId = UserContext.getUserId();
        courseService.updateCourse(teacherId, id, req);
        return Result.success();
    }

    @Operation(summary = "发布课程", description = "将课程状态变更为已发布")
    @PostMapping("/{id}/publish")
    public Result<Void> publishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        Long teacherId = UserContext.getUserId();
        courseService.publishCourse(teacherId, id);
        return Result.success();
    }

    @Operation(summary = "下架课程", description = "将课程状态变更为已下架")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        Long teacherId = UserContext.getUserId();
        courseService.unpublishCourse(teacherId, id);
        return Result.success();
    }

    @Operation(summary = "删除课程", description = "软删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        Long teacherId = UserContext.getUserId();
        courseService.deleteCourse(teacherId, id);
        return Result.success();
    }

    @Operation(summary = "获取课程详情", description = "获取课程基本信息")
    @GetMapping("/{id}")
    public Result<Course> getCourseDetail(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        return Result.success(courseService.getCourseDetail(id));
    }

    @Operation(summary = "获取课程分页列表", description = "按分类或关键字搜索课程列表")
    @GetMapping("/list")
    public Result<Page<Course>> getCourseList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getCourseList(page, size, category, keyword));
    }

    @Operation(summary = "获取课程统计报表（TODO）", description = "获取课程相关学习数据报表（暂未实现）")
    @GetMapping("/{id}/statistics")
    public Result<Void> getCourseStatistics(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        // TODO 课程统计报表待实现
        return Result.error(500, "TODO 课程统计报表待实现");
    }
}
