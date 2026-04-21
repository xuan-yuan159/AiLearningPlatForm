package org.xuanyuan.course.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xuanyuan.common.event.EventPublisher;
import org.xuanyuan.common.exception.BaseException;
import org.xuanyuan.course.dto.CourseSaveReq;
import org.xuanyuan.course.dto.CourseStatusChangeEvent;
import org.xuanyuan.course.dto.PublicCourseCatalogResp;
import org.xuanyuan.course.entity.Chapter;
import org.xuanyuan.course.entity.Course;
import org.xuanyuan.course.entity.Resource;
import org.xuanyuan.course.mapper.ChapterMapper;
import org.xuanyuan.course.mapper.CourseMapper;
import org.xuanyuan.course.mapper.ResourceMapper;
import org.xuanyuan.course.service.CourseService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private static final int COURSE_STATUS_DRAFT = 0;
    private static final int COURSE_STATUS_PUBLISHED = 1;
    private static final int COURSE_STATUS_UNPUBLISHED = 2;
    private static final int COURSE_STATUS_DELETED = 3;

    private static final String CACHE_KEY_COURSE_PREFIX = "course:";
    private static final String CACHE_KEY_LIST_PREFIX = "course:list:";

    private final StringRedisTemplate stringRedisTemplate;
    private final EventPublisher eventPublisher;
    private final ChapterMapper chapterMapper;
    private final ResourceMapper resourceMapper;

    /**
     * 创建课程草稿
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(Long teacherId, CourseSaveReq req) {
        Course course = new Course();
        course.setTeacherId(teacherId);
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setCoverUrl(req.getCoverUrl());
        course.setCategory(req.getCategory());
        course.setTags(req.getTags());
        course.setDifficulty(req.getDifficulty());
        course.setStatus(COURSE_STATUS_DRAFT);
        this.save(course);

        clearListCache(course.getCategory());
        return course.getId();
    }

    /**
     * 更新课程信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long teacherId, Long courseId, CourseSaveReq req) {
        Course course = getCourseAndCheckAuth(teacherId, courseId);
        String oldCategory = course.getCategory();

        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setCoverUrl(req.getCoverUrl());
        course.setCategory(req.getCategory());
        course.setTags(req.getTags());
        course.setDifficulty(req.getDifficulty());
        this.updateById(course);

        stringRedisTemplate.delete(CACHE_KEY_COURSE_PREFIX + courseId); // 清理课程详情缓存
        clearListCache(oldCategory); // 清理旧分类缓存
        if (req.getCategory() != null && !req.getCategory().equals(oldCategory)) {
            clearListCache(req.getCategory()); // 清理新分类缓存
        }
    }

    /**
     * 发布课程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishCourse(Long teacherId, Long courseId) {
        Course course = getCourseAndCheckAuth(teacherId, courseId);
        if (course.getStatus() == COURSE_STATUS_PUBLISHED) {
            throw new BaseException("Course is already published");
        }
        Integer oldStatus = course.getStatus();
        course.setStatus(COURSE_STATUS_PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
        this.updateById(course);

        eventPublisher.publish("course.status.change", new CourseStatusChangeEvent(courseId, oldStatus, COURSE_STATUS_PUBLISHED));
        stringRedisTemplate.delete(CACHE_KEY_COURSE_PREFIX + courseId); // 清理详情缓存
        clearListCache(course.getCategory()); // 清理列表缓存
    }

    /**
     * 下架课程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishCourse(Long teacherId, Long courseId) {
        Course course = getCourseAndCheckAuth(teacherId, courseId);
        if (course.getStatus() == COURSE_STATUS_UNPUBLISHED) {
            throw new BaseException("Course is already unpublished");
        }
        Integer oldStatus = course.getStatus();
        course.setStatus(COURSE_STATUS_UNPUBLISHED);
        this.updateById(course);

        eventPublisher.publish("course.status.change", new CourseStatusChangeEvent(courseId, oldStatus, COURSE_STATUS_UNPUBLISHED));
        stringRedisTemplate.delete(CACHE_KEY_COURSE_PREFIX + courseId); // 清理详情缓存
        clearListCache(course.getCategory()); // 清理列表缓存
    }

    /**
     * 删除课程（逻辑删除）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long teacherId, Long courseId) {
        Course course = getCourseAndCheckAuth(teacherId, courseId);
        Integer oldStatus = course.getStatus();
        course.setStatus(COURSE_STATUS_DELETED);
        this.updateById(course);

        eventPublisher.publish("course.status.change", new CourseStatusChangeEvent(courseId, oldStatus, COURSE_STATUS_DELETED));
        stringRedisTemplate.delete(CACHE_KEY_COURSE_PREFIX + courseId); // 清理详情缓存
        clearListCache(course.getCategory()); // 清理列表缓存
    }

    /**
     * 查询课程分页（通用）
     */
    @Override
    public Page<Course> getCourseList(Integer page, Integer size, String category, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(Course::getStatus, COURSE_STATUS_DELETED);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Course::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        wrapper.orderByDesc(Course::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 查询课程详情（通用）
     */
    @Override
    public Course getCourseDetail(Long courseId) {
        String cacheKey = CACHE_KEY_COURSE_PREFIX + courseId;
        String cacheStr = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheStr != null) {
            return JSON.parseObject(cacheStr, Course.class);
        }

        Course course = this.getById(courseId);
        if (course == null || course.getStatus() == COURSE_STATUS_DELETED) {
            throw new BaseException("Course not found or deleted");
        }
        stringRedisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(course), 10, TimeUnit.MINUTES);
        return course;
    }

    /**
     * 查询学生端课程分页（仅已发布）
     */
    @Override
    public Page<Course> getPublicCourseList(Integer page, Integer size, String category, String keyword) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, COURSE_STATUS_PUBLISHED);
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Course::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Course::getTitle, keyword);
        }
        wrapper.orderByDesc(Course::getPublishedAt);
        wrapper.orderByDesc(Course::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }

    /**
     * 查询学生端课程详情（仅已发布）
     */
    @Override
    public Course getPublicCourseDetail(Long courseId) {
        Course course = this.getById(courseId);
        if (course == null || course.getStatus() == COURSE_STATUS_DELETED) {
            throw new BaseException(404, "Course not found");
        }
        if (course.getStatus() != COURSE_STATUS_PUBLISHED) {
            throw new BaseException(403, "Course is not published");
        }
        return course;
    }

    /**
     * 查询学生端课程目录（章节+资源）
     */
    @Override
    public PublicCourseCatalogResp getPublicCourseCatalog(Long courseId) {
        Course course = getPublicCourseDetail(courseId);

        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .eq(Chapter::getCourseId, courseId)
                .orderByAsc(Chapter::getSortOrder)
                .orderByAsc(Chapter::getId));
        List<Resource> resources = resourceMapper.selectList(new LambdaQueryWrapper<Resource>()
                .eq(Resource::getCourseId, courseId)
                .orderByAsc(Resource::getChapterId)
                .orderByAsc(Resource::getSortOrder)
                .orderByAsc(Resource::getId));

        PublicCourseCatalogResp resp = new PublicCourseCatalogResp();
        resp.setCourseId(course.getId());
        resp.setCourseTitle(course.getTitle());

        Map<Long, PublicCourseCatalogResp.CatalogChapterDto> chapterMap = new LinkedHashMap<>();
        for (Chapter chapter : chapters) {
            PublicCourseCatalogResp.CatalogChapterDto chapterDto = new PublicCourseCatalogResp.CatalogChapterDto();
            chapterDto.setChapterId(chapter.getId());
            chapterDto.setTitle(chapter.getTitle());
            chapterDto.setSummary(chapter.getSummary());
            chapterDto.setSortOrder(chapter.getSortOrder());
            chapterMap.put(chapter.getId(), chapterDto);
        }

        boolean hasUnassigned = resources.stream().anyMatch(item -> item.getChapterId() == null);
        if (hasUnassigned) {
            PublicCourseCatalogResp.CatalogChapterDto unassigned = new PublicCourseCatalogResp.CatalogChapterDto();
            unassigned.setChapterId(0L);
            unassigned.setTitle("Unassigned");
            unassigned.setSummary("");
            unassigned.setSortOrder(Integer.MAX_VALUE);
            chapterMap.put(0L, unassigned);
        }

        for (Resource resource : resources) {
            PublicCourseCatalogResp.CatalogResourceDto resourceDto = new PublicCourseCatalogResp.CatalogResourceDto();
            resourceDto.setResourceId(resource.getId());
            resourceDto.setTitle(resource.getTitle());
            resourceDto.setType(resource.getType());
            resourceDto.setUrl(resource.getUrl());
            resourceDto.setSizeBytes(resource.getSizeBytes());
            resourceDto.setDurationS(resource.getDurationS());
            resourceDto.setSortOrder(resource.getSortOrder());

            Long chapterId = resource.getChapterId() == null ? 0L : resource.getChapterId();
            PublicCourseCatalogResp.CatalogChapterDto chapterDto = chapterMap.get(chapterId);
            if (chapterDto == null) {
                chapterDto = new PublicCourseCatalogResp.CatalogChapterDto();
                chapterDto.setChapterId(chapterId);
                chapterDto.setTitle("Unassigned");
                chapterDto.setSummary("");
                chapterDto.setSortOrder(Integer.MAX_VALUE);
                chapterMap.put(chapterId, chapterDto);
            }
            chapterDto.getResources().add(resourceDto);
        }

        resp.setChapters(new ArrayList<>(chapterMap.values()));
        return resp;
    }

    /**
     * 查询课程并校验教师权限
     */
    private Course getCourseAndCheckAuth(Long teacherId, Long courseId) {
        Course course = this.getById(courseId);
        if (course == null || course.getStatus() == COURSE_STATUS_DELETED) {
            throw new BaseException("Course not found or deleted");
        }
        if (!course.getTeacherId().equals(teacherId)) {
            throw new BaseException("No permission to operate this course");
        }
        return course;
    }

    /**
     * 清理课程列表缓存
     */
    private void clearListCache(String category) {
        String catKey = category == null ? "all" : category;
        Set<String> keys = stringRedisTemplate.keys(CACHE_KEY_LIST_PREFIX + catKey + ":*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
        Set<String> allKeys = stringRedisTemplate.keys(CACHE_KEY_LIST_PREFIX + "all:*");
        if (allKeys != null && !allKeys.isEmpty()) {
            stringRedisTemplate.delete(allKeys);
        }
    }
}
