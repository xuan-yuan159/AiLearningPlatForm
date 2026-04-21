package org.xuanyuan.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "学生首页聚合响应")
public class LearningHomeResp {

    @Schema(description = "学习汇总")
    private ProgressSummaryDto summary;

    @Schema(description = "最近学习课程")
    private List<CourseCardDto> recentLearningCourses = new ArrayList<>();

    @Schema(description = "学习中课程")
    private List<CourseCardDto> learningCourses = new ArrayList<>();

    @Schema(description = "推荐课程")
    private List<CourseCardDto> recommendedCourses = new ArrayList<>();
}
