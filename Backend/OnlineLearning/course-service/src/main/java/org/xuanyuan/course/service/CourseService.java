package org.xuanyuan.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.xuanyuan.course.dto.CourseSaveReq;
import org.xuanyuan.course.entity.Course;

public interface CourseService extends IService<Course> {

    Long createCourse(Long teacherId, CourseSaveReq req);

    void updateCourse(Long teacherId, Long courseId, CourseSaveReq req);

    void publishCourse(Long teacherId, Long courseId);

    void unpublishCourse(Long teacherId, Long courseId);

    void deleteCourse(Long teacherId, Long courseId);

    Page<Course> getCourseList(Integer page, Integer size, String category, String keyword);

    Course getCourseDetail(Long courseId);
}