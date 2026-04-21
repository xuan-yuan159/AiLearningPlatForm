package org.xuanyuan.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "我的课程分页项")
public class EnrollmentCourseDto {

    @Schema(description = "选课关系ID")
    private Long enrollmentId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "课程封面")
    private String coverUrl;

    @Schema(description = "课程分类")
    private String category;

    @Schema(description = "课程难度")
    private Integer difficulty;

    @Schema(description = "选课状态：1-学习中 2-已完成")
    private Integer status;

    @Schema(description = "进度百分比")
    private Integer progressPercent;

    @Schema(description = "资源总数")
    private Integer totalResources;

    @Schema(description = "已完成资源数")
    private Integer completedResources;

    @Schema(description = "累计学习时长（秒）")
    private Integer totalWatchedS;

    @Schema(description = "最近学习时间")
    private LocalDateTime lastStudiedAt;

    @Schema(description = "选课时间")
    private LocalDateTime enrolledAt;
}
