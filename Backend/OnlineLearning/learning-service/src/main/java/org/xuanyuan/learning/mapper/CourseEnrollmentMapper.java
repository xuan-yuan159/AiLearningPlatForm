package org.xuanyuan.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xuanyuan.learning.entity.CourseEnrollment;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

    /**
     * 查询课程热度（基于选课人数）
     */
    @Select("SELECT course_id AS courseId, COUNT(1) AS hotCount " +
            "FROM course_enrollment WHERE status IN (1,2) GROUP BY course_id")
    List<Map<String, Object>> selectHotCourseStats();
}
