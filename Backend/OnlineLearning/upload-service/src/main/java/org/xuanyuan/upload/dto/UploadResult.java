package org.xuanyuan.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResult {
    private Long resourceId;
    private String url;
    private String objectKey;
}
