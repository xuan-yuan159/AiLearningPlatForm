package org.xuanyuan.learning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.xuanyuan.learning.dto.CourseCardDto;
import org.xuanyuan.learning.dto.CourseProgressResp;
import org.xuanyuan.learning.dto.EnrollmentCourseDto;
import org.xuanyuan.learning.dto.LearningHomeResp;
import org.xuanyuan.learning.dto.StudyProgressReportReq;

import java.util.List;

public interface LearningService {

    /**
     * 学生选课（幂等）
     */
    Long enrollCourse(Long studentId, Long courseId);

    /**
     * 分页查询我的课程
     */
    Page<EnrollmentCourseDto> listMyEnrollments(Long studentId, Integer status, Integer page, Integer size);

    /**
     * 上报学习进度
     */
    void reportProgress(Long studentId, StudyProgressReportReq req);

    /**
     * 查询课程学习进度详情
     */
    CourseProgressResp getCourseProgress(Long studentId, Long courseId);

    /**
     * 查询学生首页聚合数据
     */
    LearningHomeResp getLearningHome(Long studentId);

    /**
     * 查询推荐课程
     */
    List<CourseCardDto> recommendCourses(Long studentId, Integer limit);
}
