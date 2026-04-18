package org.xuanyuan.course.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.course.entity.Chapter;
import org.xuanyuan.course.entity.Course;
import org.xuanyuan.course.entity.Resource;
import org.xuanyuan.course.mapper.ResourceMapper;
import org.xuanyuan.course.service.ChapterService;
import org.xuanyuan.course.service.CourseService;
import org.xuanyuan.course.service.ResourceService;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private final ChapterService chapterService;
    private final CourseService courseService;

    @Override
    public void downloadMaterials() {
        // TODO 课程资料打包下载
        throw new BaseException("课程资料打包下载功能待实现");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindResource(Long teacherId, Long chapterId, Long resourceId) {
        Chapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            throw new BaseException("章节不存在");
        }
        
        Course course = courseService.getById(chapter.getCourseId());
        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new BaseException("无权操作非本人创建的课程资源");
        }

        Resource resource = this.getById(resourceId);
        if (resource == null) {
            throw new BaseException("资源不存在");
        }

        resource.setChapterId(chapterId);
        this.updateById(resource);
    }
}
