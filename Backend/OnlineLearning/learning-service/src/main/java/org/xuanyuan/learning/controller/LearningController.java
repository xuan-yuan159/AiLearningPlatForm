package org.xuanyuan.learning.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.result.Result;
import org.xuanyuan.common.util.RoleUtils;
import org.xuanyuan.learning.dto.CourseCardDto;
import org.xuanyuan.learning.dto.CourseProgressResp;
import org.xuanyuan.learning.dto.EnrollmentCourseDto;
import org.xuanyuan.learning.dto.LearningHomeResp;
import org.xuanyuan.learning.dto.StudyProgressReportReq;
import org.xuanyuan.learning.service.LearningService;

import java.util.List;

@Tag(name = "学习中心", description = "学生选课、学习进度与首页聚合接口")
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    /**
     * 学生选课接口（幂等）
     */
    @Operation(summary = "学生选课", description = "学生对已发布课程进行选课，支持幂等")
    @PostMapping("/enrollments/{courseId}")
    public Result<Long> enrollCourse(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(learningService.enrollCourse(studentId, courseId));
    }

    /**
     * 分页查询我的课程
     */
    @Operation(summary = "查询我的选课列表", description = "按学习状态分页查询学生选课记录")
    @GetMapping("/enrollments/me")
    public Result<Page<EnrollmentCourseDto>> listMyEnrollments(
            @Parameter(description = "选课状态：1-学习中，2-已完成") @RequestParam(value = "status", required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(learningService.listMyEnrollments(studentId, status, page, size));
    }

    /**
     * 上报学习进度
     */
    @Operation(summary = "上报学习进度", description = "按 userId + resourceId 进行新增或更新")
    @PostMapping("/progress")
    public Result<Void> reportProgress(@RequestBody @Valid StudyProgressReportReq req) {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        learningService.reportProgress(studentId, req);
        return Result.success();
    }

    /**
     * 查询课程维度进度详情
     */
    @Operation(summary = "查询课程学习进度", description = "查询章节与资源维度的学习进度详情")
    @GetMapping("/progress/course/{courseId}")
    public Result<CourseProgressResp> getCourseProgress(
            @Parameter(description = "课程ID", required = true) @PathVariable("courseId") Long courseId) {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(learningService.getCourseProgress(studentId, courseId));
    }

    /**
     * 学生首页聚合
     */
    @Operation(summary = "获取学习首页聚合数据", description = "返回最近学习、学习中、推荐课程及学习汇总")
    @GetMapping("/home")
    public Result<LearningHomeResp> getLearningHome() {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(learningService.getLearningHome(studentId));
    }

    /**
     * 推荐课程列表
     */
    @Operation(summary = "获取推荐课程", description = "基于规则返回课程推荐列表")
    @GetMapping("/recommendations/courses")
    public Result<List<CourseCardDto>> recommendCourses(
            @Parameter(description = "返回数量，默认6，最大20") @RequestParam(value = "limit", required = false) Integer limit) {
        RoleUtils.assertStudentRole();
        Long studentId = UserContext.getUserId(); // 从网关注入头中读取当前用户ID
        return Result.success(learningService.recommendCourses(studentId, limit));
    }
}
