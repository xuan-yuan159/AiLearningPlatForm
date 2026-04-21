package org.xuanyuan.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "课程学习进度明细")
public class CourseProgressResp {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "资源总数")
    private Integer totalResources;

    @Schema(description = "已完成资源数")
    private Integer completedResources;

    @Schema(description = "课程进度百分比")
    private Integer progressPercent;

    @Schema(description = "累计学习时长（秒）")
    private Integer totalWatchedS;

    @Schema(description = "最近学习时间")
    private LocalDateTime lastStudiedAt;

    @Schema(description = "章节进度列表")
    private List<ChapterProgressDto> chapters = new ArrayList<>();

    @Data
    @Schema(description = "章节进度")
    public static class ChapterProgressDto {

        @Schema(description = "章节ID")
        private Long chapterId;

        @Schema(description = "章节标题")
        private String title;

        @Schema(description = "章节排序")
        private Integer sortOrder;

        @Schema(description = "章节资源列表")
        private List<ResourceProgressDto> resources = new ArrayList<>();
    }

    @Data
    @Schema(description = "资源进度")
    public static class ResourceProgressDto {

        @Schema(description = "资源ID")
        private Long resourceId;

        @Schema(description = "资源标题")
        private String title;

        @Schema(description = "资源类型：1-视频 2-资料")
        private Integer type;

        @Schema(description = "资源URL")
        private String url;

        @Schema(description = "资源时长（秒）")
        private Integer durationS;

        @Schema(description = "资源排序")
        private Integer sortOrder;

        @Schema(description = "当前播放进度（秒）")
        private Integer progressSec;

        @Schema(description = "累计学习时长（秒）")
        private Integer totalWatchedS;

        @Schema(description = "是否完成：0-未完成 1-已完成")
        private Integer completed;

        @Schema(description = "最近学习时间")
        private LocalDateTime lastStudiedAt;
    }
}
