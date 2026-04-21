package org.xuanyuan.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("course_enrollment")
public class CourseEnrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    /**
     * 1-学习中 2-已完成 0-取消
     */
    private Integer status;

    private LocalDateTime enrolledAt;

    private LocalDateTime updatedAt;
}
