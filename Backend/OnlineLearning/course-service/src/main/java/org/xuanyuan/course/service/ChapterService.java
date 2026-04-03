package org.xuanyuan.course.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.xuanyuan.course.dto.ChapterSaveReq;
import org.xuanyuan.course.entity.Chapter;

import java.util.List;

public interface ChapterService extends IService<Chapter> {

    Long createChapter(Long teacherId, ChapterSaveReq req);

    void updateChapter(Long teacherId, Long chapterId, ChapterSaveReq req);

    void deleteChapter(Long teacherId, Long chapterId);

    List<Chapter> getChapterList(Long courseId);
}