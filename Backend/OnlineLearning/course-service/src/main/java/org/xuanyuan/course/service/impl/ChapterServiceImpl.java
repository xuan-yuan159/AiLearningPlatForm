package org.xuanyuan.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.course.dto.ChapterSaveReq;
import org.xuanyuan.course.entity.Chapter;
import org.xuanyuan.course.entity.Course;
import org.xuanyuan.course.mapper.ChapterMapper;
import org.xuanyuan.course.service.ChapterService;
import org.xuanyuan.course.service.CourseService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChapterServiceImpl extends ServiceImpl<ChapterMapper, Chapter> implements ChapterService {

    private final CourseService courseService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChapter(Long teacherId, ChapterSaveReq req) {
        checkCourseAuth(teacherId, req.getCourseId());
        
        Chapter chapter = new Chapter();
        chapter.setCourseId(req.getCourseId());
        chapter.setTitle(req.getTitle());
        chapter.setSummary(req.getSummary());
        chapter.setSortOrder(req.getSortOrder());
        this.save(chapter);
        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(Long teacherId, Long chapterId, ChapterSaveReq req) {
        Chapter chapter = this.getById(chapterId);
        if (chapter == null) {
            throw new BaseException("章节不存在");
        }
        checkCourseAuth(teacherId, chapter.getCourseId());
        
        chapter.setTitle(req.getTitle());
        chapter.setSummary(req.getSummary());
        chapter.setSortOrder(req.getSortOrder());
        this.updateById(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long teacherId, Long chapterId) {
        Chapter chapter = this.getById(chapterId);
        if (chapter == null) {
            throw new BaseException("章节不存在");
        }
        checkCourseAuth(teacherId, chapter.getCourseId());
        this.removeById(chapterId);
    }

    @Override
    public List<Chapter> getChapterList(Long courseId) {
        LambdaQueryWrapper<Chapter> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Chapter::getCourseId, courseId);
        wrapper.orderByAsc(Chapter::getSortOrder);
        return this.list(wrapper);
    }

    private void checkCourseAuth(Long teacherId, Long courseId) {
        Course course = courseService.getById(courseId);
        if (course == null || course.getStatus() == 3) {
            throw new BaseException("课程不存在或已删除");
        }
        if (!course.getTeacherId().equals(teacherId)) {
            throw new BaseException("无权操作非本人创建的课程资源");
        }
    }
}