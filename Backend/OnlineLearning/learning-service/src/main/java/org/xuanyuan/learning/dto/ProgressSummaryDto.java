package org.xuanyuan.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "学习汇总信息")
public class ProgressSummaryDto {

    @Schema(description = "学习中课程数")
    private Integer learningCourseCount;

    @Schema(description = "已完成课程数")
    private Integer completedCourseCount;

    @Schema(description = "累计学习时长（秒）")
    private Integer totalWatchedS;
}
