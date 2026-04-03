package org.xuanyuan.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xuanyuan.course.entity.Course;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}