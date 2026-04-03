package org.xuanyuan.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private String title;

    private String description;

    private String coverUrl;

    private String category;

    private String tags;

    private Integer difficulty;

    /**
     * 0-草稿 1-已发布 2-已下架 3-已删除 (逻辑删除)
     */
    private Integer status;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}