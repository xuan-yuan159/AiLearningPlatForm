package org.xuanyuan.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xuanyuan.course.entity.Resource;

public interface ResourceService extends IService<Resource> {

    // TODO 课程资料打包下载
    void downloadMaterials();

    void bindResource(Long teacherId, Long chapterId, Long resourceId);
}
