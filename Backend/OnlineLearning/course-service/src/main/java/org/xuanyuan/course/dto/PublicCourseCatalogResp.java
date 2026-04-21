package org.xuanyuan.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "学生端课程目录响应")
public class PublicCourseCatalogResp {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程标题")
    private String courseTitle;

    @Schema(description = "章节目录")
    private List<CatalogChapterDto> chapters = new ArrayList<>();

    @Data
    @Schema(description = "目录章节")
    public static class CatalogChapterDto {

        @Schema(description = "章节ID")
        private Long chapterId;

        @Schema(description = "章节标题")
        private String title;

        @Schema(description = "章节摘要")
        private String summary;

        @Schema(description = "章节排序")
        private Integer sortOrder;

        @Schema(description = "章节资源列表")
        private List<CatalogResourceDto> resources = new ArrayList<>();
    }

    @Data
    @Schema(description = "目录资源")
    public static class CatalogResourceDto {

        @Schema(description = "资源ID")
        private Long resourceId;

        @Schema(description = "资源标题")
        private String title;

        @Schema(description = "资源类型：1-视频 2-资料")
        private Integer type;

        @Schema(description = "资源链接")
        private String url;

        @Schema(description = "资源大小（字节）")
        private Long sizeBytes;

        @Schema(description = "资源时长（秒）")
        private Integer durationS;

        @Schema(description = "资源排序")
        private Integer sortOrder;
    }
}
