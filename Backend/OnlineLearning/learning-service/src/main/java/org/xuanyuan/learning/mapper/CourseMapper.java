package org.xuanyuan.learning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.xuanyuan.learning.entity.Course;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
