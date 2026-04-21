package org.xuanyuan.learning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long courseId;

    private Long chapterId;

    private Long resourceId;

    private Integer progressSec;

    private Integer totalWatchedS;

    /**
     * 0-未完成 1-已完成
     */
    private Integer completed;

    private LocalDateTime lastStudiedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
