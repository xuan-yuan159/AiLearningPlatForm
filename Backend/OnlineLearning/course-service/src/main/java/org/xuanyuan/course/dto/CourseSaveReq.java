package org.xuanyuan.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "课程创建/编辑请求对象")
public class CourseSaveReq {
    @Schema(description = "课程标题", required = true)
    @NotBlank(message = "课程标题不能为空")
    private String title;

    @Schema(description = "课程简介")
    private String description;

    @Schema(description = "封面URL")
    private String coverUrl;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "标签(逗号分隔)")
    private String tags;

    @Schema(description = "难度(1-入门 2-进阶 3-高级)")
    private Integer difficulty;
}