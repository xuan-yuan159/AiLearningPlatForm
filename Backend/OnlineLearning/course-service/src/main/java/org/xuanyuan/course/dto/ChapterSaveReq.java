package org.xuanyuan.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "章节创建/编辑请求对象")
public class ChapterSaveReq {
    @Schema(description = "课程ID", required = true)
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @Schema(description = "章节标题", required = true)
    @NotBlank(message = "章节标题不能为空")
    private String title;

    @Schema(description = "章节简介")
    private String summary;

    @Schema(description = "排序", required = true)
    @NotNull(message = "排序不能为空")
    private Integer sortOrder;
}