package org.xuanyuan.upload.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource")
public class Resource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long chapterId;

    private Integer type;

    private String title;

    private String url;

    private Long sizeBytes;

    private Integer durationS;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
