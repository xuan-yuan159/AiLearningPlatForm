package org.xuanyuan.learning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "学习进度上报请求")
public class StudyProgressReportReq {

    @NotNull(message = "courseId 不能为空")
    @Schema(description = "课程ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @Schema(description = "章节ID（可选）")
    private Long chapterId;

    @NotNull(message = "resourceId 不能为空")
    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resourceId;

    @NotNull(message = "progressSec 不能为空")
    @Min(value = 0, message = "progressSec 不能小于0")
    @Schema(description = "当前播放进度（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer progressSec;

    @NotNull(message = "totalWatchedS 不能为空")
    @Min(value = 0, message = "totalWatchedS 不能小于0")
    @Schema(description = "本次新增学习时长（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalWatchedS;

    @NotNull(message = "completed 不能为空")
    @Schema(description = "本资源是否完成", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean completed;
}
