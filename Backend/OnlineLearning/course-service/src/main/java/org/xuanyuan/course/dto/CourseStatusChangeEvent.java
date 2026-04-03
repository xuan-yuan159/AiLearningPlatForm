package org.xuanyuan.course.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import org.xuanyuan.common.event.BaseEvent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusChangeEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private Long courseId;
    
    private Integer oldStatus;
    
    private Integer newStatus;
}