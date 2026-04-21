package org.xuanyuan.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.common.util.RoleUtils;
import org.xuanyuan.course.dto.CourseSaveReq;
import org.xuanyuan.course.dto.PublicCourseCatalogResp;
import org.xuanyuan.course.entity.Course;
import org.xuanyuan.course.service.CourseService;

@Tag(name = "课程管理", description = "教师课程管理与学生查询接口")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 创建课程草稿（教师）
     */
    @Operation(summary = "创建课程", description = "教师创建课程草稿")
    @PostMapping
    public Result<Long> createCourse(@RequestBody @Valid CourseSaveReq req) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(courseService.createCourse(teacherId, req));
    }

    /**
     * 修改课程信息（教师）
     */
    @Operation(summary = "修改课程", description = "教师修改课程信息")
    @PutMapping("/{id}")
    public Result<Void> updateCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id,
            @RequestBody @Valid CourseSaveReq req) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        courseService.updateCourse(teacherId, id, req);
        return Result.success();
    }

    /**
     * 发布课程（教师）
     */
    @Operation(summary = "发布课程", description = "教师发布课程")
    @PostMapping("/{id}/publish")
    public Result<Void> publishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        courseService.publishCourse(teacherId, id);
        return Result.success();
    }

    /**
     * 下架课程（教师）
     */
    @Operation(summary = "下架课程", description = "教师下架课程")
    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublishCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        courseService.unpublishCourse(teacherId, id);
        return Result.success();
    }

    /**
     * 删除课程（教师）
     */
    @Operation(summary = "删除课程", description = "教师软删除课程")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        RoleUtils.assertTeacherRole();
        Long teacherId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        courseService.deleteCourse(teacherId, id);
        return Result.success();
    }

    /**
     * 查询课程详情（通用）
     */
    @Operation(summary = "查询课程详情", description = "查询课程基础信息")
    @GetMapping("/{id}")
    public Result<Course> getCourseDetail(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        return Result.success(courseService.getCourseDetail(id));
    }

    /**
     * 查询课程分页（通用）
     */
    @Operation(summary = "查询课程分页", description = "按分类或关键字查询课程分页")
    @GetMapping("/list")
    public Result<Page<Course>> getCourseList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getCourseList(page, size, category, keyword));
    }

    /**
     * 学生端课程分页（仅已发布）
     */
    @Operation(summary = "查询学生端课程分页", description = "仅返回已发布课程")
    @GetMapping("/public/list")
    public Result<Page<Course>> getPublicCourseList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "分类") @RequestParam(required = false) String category,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword) {
        return Result.success(courseService.getPublicCourseList(page, size, category, keyword));
    }

    /**
     * 学生端课程详情（仅已发布）
     */
    @Operation(summary = "查询学生端课程详情", description = "仅返回已发布课程详情")
    @GetMapping("/public/{id}")
    public Result<Course> getPublicCourseDetail(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        return Result.success(courseService.getPublicCourseDetail(id));
    }

    /**
     * 学生端课程目录（章节+资源）
     */
    @Operation(summary = "查询学生端课程目录", description = "返回学生端课程的章节与资源目录")
    @GetMapping("/public/{id}/catalog")
    public Result<PublicCourseCatalogResp> getPublicCourseCatalog(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        return Result.success(courseService.getPublicCourseCatalog(id));
    }

    /**
     * 课程统计（教师，TODO）
     */
    @Operation(summary = "查询课程统计（TODO）", description = "课程统计功能暂未实现")
    @GetMapping("/{id}/statistics")
    public Result<Void> getCourseStatistics(
            @Parameter(description = "课程ID", required = true) @PathVariable("id") Long id) {
        RoleUtils.assertTeacherRole();
        return Result.error(500, "TODO: course statistics is not implemented");
    }
}
